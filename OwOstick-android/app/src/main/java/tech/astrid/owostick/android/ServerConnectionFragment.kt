package tech.astrid.owostick.android

import android.os.Bundle
import android.util.Log
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
import java.util.*

class ServerConnectionFragment : Fragment() {
    private lateinit var binding: FragmentServerConnectionBinding
    private val logTag = ServerConnectionFragment::class.qualifiedName!!

    private val connection =
        BehaviorSubject.createDefault<Optional<ServerConnection>>(Optional.empty())

    private val _state = connection
        .switchMap { opt ->
            opt.map { sc ->
                sc.state
                    .map {
                        when (it) {
                            ServerConnection.State.Unauthenticated,
                            ServerConnection.State.Connecting -> State.Connecting(sc)
                            is ServerConnection.State.Authenticated -> State.Connected(sc)
                            is ServerConnection.State.Disconnected -> State.Disconnected
                        }
                    }
            }.orElse(Observable.just(State.Disconnected))
        }

    val power: Observable<Float> = connection
        .filter { it.isPresent }
        .switchMap { it.get().power }

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
                .filter { (_, _, state) -> state is State.Disconnected }
                .subscribe { (host, pass, state) ->
                    Log.i(
                        logTag,
                        "Connecting to server with host=${host} pass=${pass} state=${state}"
                    )
                    val uri = URI("ws://$host/api/device")
                    val client = ServerConnection(uri, pass.toString())
                    connection.onNext(Optional.of(client))
                }

            state.subscribe {
                requireActivity().runOnUiThread {
                    connectToServer.isEnabled = it !is State.Connecting
                    connectToServer.text = when (it) {
                        is State.Disconnected -> "Connect"
                        is State.Connecting -> "Connecting..."
                        is State.Connected -> "Disconnect"
                    }
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