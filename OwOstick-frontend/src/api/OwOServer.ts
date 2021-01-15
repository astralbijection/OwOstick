import {
  BehaviorSubject,
  combineLatest,
  Observable,
  Observer,
  Subject,
} from "rxjs";
import { combineAll, distinctUntilChanged, filter, map } from "rxjs/operators";

type Action<T = any> = T & { type: string };
type Event<T = any> = T & { type: string };
type DeviceConnection = Event<{ value: boolean }>;

type State =
  | {
      state: "disconnected";
    }
  | {
      state: "unauthenticated";
    }
  | {
      state: "authenticated";
    };

export class OwOServer {
  private readonly socket: WebSocket;

  public readonly destinationConnected$: Observable<boolean>;
  public readonly isReady$: Observable<boolean>;

  private readonly state$: BehaviorSubject<State>;
  private readonly message$: Subject<Event>;

  get subjectState(): Observable<boolean> {
    return this.destinationConnected$;
  }

  constructor(private password: string, private socketEndpoint: string) {
    this.socket = new WebSocket(socketEndpoint);
    this.state$ = new BehaviorSubject<State>({ state: "disconnected" });

    // Socket events
    this.socket.onopen = (ev) => {
      this.state$.next({ state: "unauthenticated" });
      this.send({ type: "authenticate", value: this.password });
    };
    this.message$ = new Subject();
    this.socket.onmessage = (ev) => {
      const data = JSON.parse(ev.data) as Event;
      this.message$.next(data);
    };
    this.socket.onclose = (ev) => {
      this.state$.next({ state: "disconnected" });
    };

    const messages = combineLatest([this.message$, this.state$]);

    this.isReady$ = this.state$.pipe(
      map(({ state }) => state === "authenticated"),
      distinctUntilChanged()
    );

    // Authentication success
    messages
      .pipe(
        filter(
          ([{ type }, { state }]) =>
            type === "authentication" && state === "unauthenticated"
        )
      )
      .subscribe(([{ value }]) => {
        if (value) {
          this.state$.next({ state: "authenticated" });
        } else {
          this.close();
        }
      });

    const authenticatedMessages = messages.pipe(
      filter(([, { state }]) => state === "authenticated")
    );

    // Device state
    this.destinationConnected$ = authenticatedMessages.pipe(
      filter(([{ type }]) => type === "device_state"),
      map(([msg]) => (msg as DeviceConnection).value)
    );
  }

  private send(obj: Action) {
    this.socket.send(JSON.stringify(obj));
  }

  sendPower(power: number) {
    this.send({
      action: "set_power",
      value: power,
    });
  }

  close() {
    this.socket.close();
  }
}
