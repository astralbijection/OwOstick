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

fn set_client(client: SingleClient, ws: warp::ws::Ws) -> impl warp::Reply {
    let client = client.clone();
    ws.on_upgrade(|socket: warp::ws::WebSocket| async move {
        *client.lock().unwrap() = Some(Client { socket });
    })
}

fn handle_singleton_ws_client(
    client: SingleClient,
) -> warp::filters::BoxedFilter<(impl warp::Reply,)> {
    let c1 = client.clone();
    warp::any()
        .and_then(move || ensure_singleton_client(client.clone()))
        .and(warp::ws())
        .map(move |_, ws: warp::ws::Ws| set_client(c1.clone(), ws))
        .boxed()
}

#[tokio::main]
async fn main() {
    let controller: SingleClient = Arc::new(Mutex::new(None));
    let target: SingleClient = Arc::new(Mutex::new(None));

    let controller_path = warp::path("controller").and(handle_singleton_ws_client(controller));

    let target_path = warp::path("target").and(handle_singleton_ws_client(target));

    warp::serve(warp::path("api").and(controller_path.or(target_path)))
        .run(([0, 0, 0, 0], 3030))
        .await;
}
