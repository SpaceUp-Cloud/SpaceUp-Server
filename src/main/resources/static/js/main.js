import ReconnectingEventSource from "./modules/ReconnectingSSE.js";

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

function loadTheme(mode) {

    console.debug("Use " + mode + " theme");
    $.ajax({
        url: "/style/" + mode + ".css",
        dataType: 'text',
        success: function(data) {
            // Remove old style
            $("head").children().each(function(index, ele) {
                if (ele.innerHTML && ele.innerHTML.substring(0, 30).match(/\/\*current theme\*\//)) {
                    $(ele).remove();
                    return false;    // Stop iterating since we removed something
                }
            });
            $('<style type="text/css">/*current theme*/ \n' + data + '</style>').appendTo("head");
        }
    });
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
