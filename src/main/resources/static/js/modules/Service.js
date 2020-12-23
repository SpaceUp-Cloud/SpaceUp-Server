function serviceExecute(name, option) {
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
    }).done(function (response) {
        console.debug(response);
        popup(response);
    })
}