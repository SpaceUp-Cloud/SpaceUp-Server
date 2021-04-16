import {WsServiceHandler} from "./modules/Service.js";

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

function initWs() {
    let wsServiceHandler = new WsServiceHandler();
    wsServiceHandler.init();
}

// ----------------------------------
// -------------- MAIN --------------
// ----------------------------------

function init() {

    // Initialize materialize styling
    initMaterialize();

    // Activating light/dark mode events + theming
    handleBrowserModeEvent();

    // Init Websocket handlers
    initWs();
}

window.onload = init;
