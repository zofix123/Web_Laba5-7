(function () {
    const input = document.getElementById('avatarFile');
    if (!input) return;

    input.addEventListener('change', function (e) {
        const file = e.target.files && e.target.files[0];
        if (!file) return;

        const reader = new FileReader();
        reader.onload = function (ev) {
            const img = document.getElementById('avatarPreview');
            const placeholder = document.getElementById('avatarPlaceholder');

            if (placeholder) placeholder.style.display = 'none';

            if (img) {
                img.style.display = 'block';
                img.src = ev.target.result;
            } else {
                const wrap = document.querySelector('.avatar-block__preview');
                if (!wrap) return;
                const newImg = document.createElement('img');
                newImg.id = 'avatarPreview';
                newImg.className = 'avatar-block__img';
                newImg.alt = 'Аватар';
                newImg.src = ev.target.result;
                wrap.appendChild(newImg);
            }
        };
        reader.readAsDataURL(file);
    });
})();