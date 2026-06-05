(function () {
    if (!document.body || document.body.dataset.hhLiveSync === 'off') {
        return;
    }
    var path = window.location.pathname || '';
    if (path.indexOf('/candidat') !== 0 && path.indexOf('/recruteur') !== 0) {
        return;
    }
    var current = null;
    function poll() {
        fetch('/api/live/version', { credentials: 'same-origin', headers: { Accept: 'application/json' } })
            .then(function (r) { return r.ok ? r.json() : null; })
            .then(function (data) {
                if (!data || data.version === undefined) {
                    return;
                }
                if (current !== null && String(current) !== String(data.version)) {
                    window.location.reload();
                }
                current = data.version;
            })
            .catch(function () { /* ignore */ });
    }
    poll();
    setInterval(poll, 4000);
})();
