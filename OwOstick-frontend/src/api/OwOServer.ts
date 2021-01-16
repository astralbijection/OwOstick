import {
  asyncScheduler,
  BehaviorSubject,
  combineLatest,
  Observable,
  Observer,
  Subject,
} from "rxjs";
import {
  combineAll,
  distinctUntilChanged,
  filter,
  map,
  throttle,
  throttleTime,
} from "rxjs/operators";

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
  private readonly inputPower$: Subject<number>;

  get subjectState(): Observable<boolean> {
    return this.destinationConnected$;
  }

  constructor(private password: string, private socketEndpoint: string) {
    this.socket = new WebSocket(socketEndpoint);
    this.state$ = new BehaviorSubject<State>({ state: "disconnected" });

    // Socket events
    this.socket.onopen = () => {
      console.log("Socket opened, sending authentication request");
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

    this.inputPower$ = new Subject();
    const inputMessages$ = this.inputPower$.pipe(
      distinctUntilChanged(),
      throttleTime(100, asyncScheduler, { leading: false, trailing: true }),
      map(
        (power) =>
          ({
            type: "set_power",
            value: power,
          } as Action)
      )
    );
    inputMessages$.subscribe((msg) => this.send(msg));

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
          console.log("Authentication success");
          this.state$.next({ state: "authenticated" });
        } else {
          console.error("Authentication failure");
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
    console.debug("Sending object", obj);
    this.socket.send(JSON.stringify(obj));
  }

  sendPower(power: number) {
    this.inputPower$.next(power);
  }

  close() {
    this.socket.close();
  }
}
