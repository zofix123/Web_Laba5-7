(function () {
    const toastType = /*[[${toast}]]*/ '';
    const el = document.getElementById('toast');
    if (!el || !toastType) return;

    let text = '';
    if (toastType === 'liked') text = 'Вы лайкнули пользователя';
    if (toastType === 'disliked') text = 'Вы скрыли анкету';
    if (toastType === 'empty') text = 'Анкеты закончились';

    if (!text) return;

    el.textContent = text;
    el.classList.add('toast--show');

    setTimeout(() => el.classList.remove('toast--show'), 2000);
})();