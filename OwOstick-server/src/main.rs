use futures::{FutureExt, StreamExt};
use std::sync::Arc;
use std::sync::Mutex;
use warp::Filter;

struct Client {
    socket: warp::ws::WebSocket,
}

type SingleClient = Arc<Mutex<Option<Client>>>;

async fn ensure_singleton_client(
    client: SingleClient,
) -> std::result::Result<((),), warp::Rejection> {
    let pt = client.lock().unwrap();
    match *pt {
        Some(_) => Err(warp::reject::not_found()),
        None => Ok(((),)),
    }
}

#[tokio::main]
async fn main() {
    let controller: SingleClient = Arc::new(Mutex::new(None));
    let target: SingleClient = Arc::new(Mutex::new(None));

    let c1 = controller.clone();
    let controller_path = warp::path("controller")
        .and_then(move || ensure_singleton_client(controller.clone()))
        .and(warp::ws())
        .map(move |_, ws: warp::ws::Ws| {
            let controller = c1.clone();
            ws.on_upgrade(|socket: warp::ws::WebSocket| async move {
                *controller.lock().unwrap() = Some(Client { socket });
            })
        });

    let target_path = warp::path("target")
        .and_then(move || ensure_singleton_client(target.clone()))
        .and(warp::ws())
        .map(|_, ws: warp::ws::Ws| {
            ws.on_upgrade(|socket: warp::ws::WebSocket| {
                let (tx, rx) = socket.split();
                rx.forward(tx).map(|_| {})
            })
        });

    warp::serve(warp::path("api").and(controller_path.or(target_path)))
        .run(([0, 0, 0, 0], 3030))
        .await;
}
