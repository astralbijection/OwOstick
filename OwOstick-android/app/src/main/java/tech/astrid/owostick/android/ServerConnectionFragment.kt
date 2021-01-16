package tech.astrid.owostick.android

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.jakewharton.rxbinding4.view.clicks
import com.jakewharton.rxbinding4.widget.textChanges
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.kotlin.Observables.combineLatest
import io.reactivex.rxjava3.subjects.BehaviorSubject
import tech.astrid.owostick.android.databinding.FragmentServerConnectionBinding
import java.net.URI

class ServerConnectionFragment : Fragment() {
    private lateinit var binding: FragmentServerConnectionBinding

    private val _state = BehaviorSubject.createDefault<State>(
        State.Disconnected
    )
    private val connection = _state
        .switchMap { state ->
            when (state) {
                is State.Disconnected -> Observable.just(null)
                is State.Connecting -> Observable.just(null)
                is State.Connected -> state.connection.state.map {
                    if (it is ServerConnection.State.Authenticated)
                        state.connection
                    else
                        null
                }
            }
        }
        .distinctUntilChanged()

    val power: Observable<Float> = connection
        .filter { it != null }
        .switchMap { it!!.power }

    val state: Observable<State> get() = _state

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentServerConnectionBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            connectToServer.clicks()
                .withLatestFrom(
                    combineLatest(
                        serverHost.textChanges(),
                        password.textChanges(),
                        state
                    ),
                    { _, b -> b }
                )
                .subscribe { (host, pass, state) ->
                    if (state is State.Disconnected) {
                        val uri = URI("ws://$host/api/device")
                        val client = ServerConnection(uri, pass.toString())
                        _state.onNext(State.Connecting(client))
                    }
                }

        }
    }

    sealed class State {
        object Disconnected : State()
        data class Connecting(val connection: ServerConnection) : State()
        data class Connected(val connection: ServerConnection) : State()
    }

    companion object {
        @JvmStatic
        fun newInstance() = ServerConnectionFragment()
    }
}