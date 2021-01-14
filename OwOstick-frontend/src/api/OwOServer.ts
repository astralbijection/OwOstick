import { Observable, Observer, Subject } from "rxjs";

type Action<T = any> = T & { type: string };
type Event<T = any> = T & { type: string };

export class OwOServer {
  private readonly socket: WebSocket;

  public readonly destination$: Subject<boolean>;
  public readonly connected$: Subject<boolean>;

  get subjectState(): Observable<boolean> {
    return this.destination$;
  }

  constructor(private password: string, private socketEndpoint: string) {
    this.socket = new WebSocket(socketEndpoint, ["access_token", password]);
    this.destination$ = new Subject();
    this.connected$ = new Subject();
    this.connected$.next(false);

    this.socket.onopen = (ev) => {
      this.connected$.next(true);
    };

    this.socket.onmessage = (ev) => {
      const data = JSON.parse(ev.data) as Event;
      switch (data.type) {
        case "subject_state":
          this.destination$.next(data.state);
          break;
      }
    };

    this.socket.onclose = (ev) => {
      this.connected$.next(false);
    };
  }

  get isReady(): boolean {
    return this.socket.readyState === WebSocket.OPEN;
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
