import ReconnectingEventSource from "./modules/ReconnectingSSE.js";
import SseDomainHandler from "./modules/Domain.js"
import SseServiceHandler from "./modules/Service.js";


/**
 * Initialize Materialize
 */
function initMaterialize() {
    M.AutoInit(undefined);
}

/**
 * Handle the browser mode event for light/dark mode
 */
function handleBrowserModeEvent() {
    // Event handling for dark/light mode in browser
    let matchMediaDarkMode = window.matchMedia('(prefers-color-scheme: dark)');

    // Activate custom CSS styling
    if(matchMediaDarkMode.matches) {
        loadTheme("dark");
    } else {
        loadTheme("light");
    }

    $(document).ready(function() {
        matchMediaDarkMode.addEventListener('change', event => {
                if (event.matches) {
                    //dark mode
                    loadTheme("dark");
                } else {
                    //light mode
                    loadTheme("light");
                }
            })
    })
}

/**
 * Enable SSE on client side
 */
function handleSSE() {
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

// ----------------------------------
// -------------- MAIN --------------
// ----------------------------------

function init() {

    // Initialize materialize styling
    initMaterialize();

    // Activating light/dark mode events + theming
    handleBrowserModeEvent();

    // Activate SSE
    handleSSE();
}

window.onload = init;
