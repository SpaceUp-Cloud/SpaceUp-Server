import ReconnectingEventSource from "./modules/ReconnectingSSE.js";

function init() {
    let esSupport = (window.EventSource !== undefined);
    let result = document.getElementById("result");
    if(esSupport) {
        let sse = new ReconnectingEventSource("/time/events", { withCredentials: false });

              sse.addEventListener("update", function(e) {
                  console.log(e);
                  document.getElementById("time").innerHTML = event.data;
              });

              sse.onerror = function(e) {
                e = e || event;

                switch( e.target.readyState ){
                  // if reconnecting
                  case EventSource.CONNECTING:
                    console.info('Reconnecting…');
                    break;
                  // if error was fatal
                  case EventSource.CLOSED:
                    console.error('Connection failed. Will not retry.');
                    break;
                   case EventSource.OPEN:
                    break;
                }
              };
    } else {
        result.innerHTML = "Your browser doesn't support server-sent events.";
    }
}

window.onload = init;

