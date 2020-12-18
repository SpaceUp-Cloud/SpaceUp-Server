import ReconnectingEventSource from "./modules/ReconnectingSSE.js";

function initMaterialize() {
    M.AutoInit(undefined);
}

function init() {
    initMaterialize();

    let esSupport = (window.EventSource !== undefined);
    let result = document.getElementById("result");
    if(esSupport) {
        let sse = new ReconnectingEventSource("/sse/events", { withCredentials: false });

              sse.addEventListener("update", function(e) {
                  console.log(e);
                  document.getElementById("time").innerHTML = e.data;
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

