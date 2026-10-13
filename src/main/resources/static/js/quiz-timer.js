/* Countdown for the quiz-taking page.
   This is only a DISPLAY and a convenience: the SERVER stores the real deadline and decides
   what is accepted. When the countdown reaches zero the form is submitted automatically. */
(function () {
    'use strict';
    document.addEventListener('DOMContentLoaded', function () {
        var timerEl = document.getElementById('timer');
        var form = document.getElementById('quizForm');
        if (!timerEl || !form) { return; }

        var remaining = parseInt(timerEl.dataset.seconds, 10);
        // performance.now() is not affected by changing the computer's clock
        var endAt = performance.now() + remaining * 1000;
        var submitted = false;

        function pad(n) { return (n < 10 ? '0' : '') + n; }

        function tick() {
            var left = Math.max(0, Math.round((endAt - performance.now()) / 1000));
            timerEl.textContent = pad(Math.floor(left / 60)) + ':' + pad(left % 60);
            timerEl.classList.toggle('warn', left <= 120 && left > 30);
            timerEl.classList.toggle('danger', left <= 30);
            if (left <= 0 && !submitted) {
                submitted = true;
                clearInterval(id);
                document.getElementById('timeUpMessage').classList.remove('d-none');
                form.submit();
            }
        }

        // count answered questions
        var counter = document.getElementById('answeredCount');
        function updateCount() {
            var names = {};
            form.querySelectorAll('input[type="radio"]:checked').forEach(function (r) { names[r.name] = true; });
            counter.textContent = Object.keys(names).length;
        }
        form.addEventListener('change', updateCount);

        form.addEventListener('submit', function (e) {
            if (submitted) { return; }                 // automatic submit: no question
            var total = parseInt(counter.dataset.total, 10);
            var answered = parseInt(counter.textContent, 10);
            var msg = answered < total
                ? 'You have ' + (total - answered) + ' unanswered question(s). Submit anyway?'
                : 'Submit your answers now?';
            if (!window.confirm(msg)) { e.preventDefault(); return; }
            submitted = true;
        });

        var id = setInterval(tick, 250);
        updateCount();
        tick();
    });
})();
