export default class SseServiceHandler {

    constructor() {}

    serviceExecution(feedback) {
        popup(JSON.parse(feedback))
    }
}

export function serviceExecute(name, option) {
    let url = '/api/service/execute/' + name + '/' + option;
    $.ajax({
        url: url,
        method: 'post',
        beforeSend: function () {
            enableLoader(name)
        },
        complete: function () {
            disableLoader(name)
        }
    });
}