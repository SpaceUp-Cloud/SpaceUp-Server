import ReconnectingEventSource from "./modules/ReconnectingSSE.js";
import SseDomainHandler from "./modules/Domain.js"
import SseServiceHandler from "./modules/Service.js";

$(document).ready(function() {
    window.matchMedia('(prefers-color-scheme: dark)')
        .addEventListener('change', event => {
            if (event.matches) {
                //dark mode
                loadTheme("dark");
            } else {
                //light mode
                loadTheme("light");
            }
        })
})

function initMaterialize() {
    M.AutoInit(undefined);
}

function init() {
    initMaterialize();

    if(window.matchMedia('(prefers-color-scheme: dark)').matches) {
        loadTheme("dark");
    } else {
        loadTheme("light");
    }

    let esSupport = (window.EventSource !== undefined);
    let result = document.getElementById("result");
    if(esSupport) {
        let sse = new ReconnectingEventSource("/api/sse/events", { withCredentials: false });

              const sseDomainHandler = new SseDomainHandler();
              sse.addEventListener("domain add", (e) => sseDomainHandler.handleDomainAdd(e.data));
              sse.addEventListener("domain delete", (e) => sseDomainHandler.handleDomainDelete(e.data));

              const sseServiceHandler = new SseServiceHandler()
              sse.addEventListener("service exec", (e) => sseServiceHandler.serviceExecution(e.data))

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
                       console.log("SSE Connected")
                    break;
                }
              };

    } else {
        result.innerHTML = "Your browser doesn't support server-sent events.";
    }
}

window.onload = init;
