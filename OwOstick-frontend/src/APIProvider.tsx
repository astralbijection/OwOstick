import { createContext, FC, PropsWithChildren, useContext } from "react";
import { BehaviorSubject, Observable, of, Subject } from "rxjs";
import { filter, map, mergeMap, switchMap } from "rxjs/operators";
import { OwOServer } from "./api/OwOServer";

export type APIContextData = {
  server$: Observable<OwOServer | null>;
  connect: (password: string) => void;
};
const APIContext = createContext({} as APIContextData);

export const useServer = () => useContext(APIContext);

export type APIProviderProps = PropsWithChildren<{ endpoint: string }>;

export const APIProvider: FC<APIProviderProps> = ({ children, endpoint }) => {
  const serverObj$ = new BehaviorSubject<OwOServer | null>(null);

  const connect = (password: string) => {
    serverObj$.next(new OwOServer(password, endpoint));
  };

  const server$ = serverObj$.pipe(
    switchMap((server) =>
      server
        ? server.isReady$.pipe(map((state) => (state ? server : null)))
        : of(null)
    )
  );

  return (
    <APIContext.Provider
      value={{
        server$,
        connect,
      }}
    >
      {children}
    </APIContext.Provider>
  );
};

export default APIProvider;
