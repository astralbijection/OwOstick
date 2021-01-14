import { createContext, FC, PropsWithChildren, useContext } from "react";
import { Observable, of, Subject } from "rxjs";
import { map, mergeMap, switchMap } from "rxjs/operators";
import { OwOServer } from "./api/OwOServer";
export type APIContextData = {
  server$: Observable<OwOServer | null>;
  connect(password: string): void;
};
const APIContext = createContext({} as APIContextData);

export const useServer = () => useContext(APIContext);

export type APIProviderProps = PropsWithChildren<{ endpoint: string }>;

export const APIProvider: FC<APIProviderProps> = ({ children, endpoint }) => {
  const server$ = new Subject<OwOServer | null>();

  const connect = (password: string) => {
    server$.next(new OwOServer(password, endpoint));
  };

  const connectedServer$ = server$.pipe(
    switchMap((server) => {
      if (server == null) return of(false);
      return server.connected$;
    }),
    switchMap((connected) => (connected ? server$ : of(null)))
  );

  return (
    <APIContext.Provider value={{ server$: connectedServer$, connect }}>
      {children}
    </APIContext.Provider>
  );
};

export default APIProvider;
