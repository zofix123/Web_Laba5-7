function openEditModal(id, name, email, birth, role, gender, city) {
    document.getElementById('editName').value = name || '';
    document.getElementById('editEmail').value = email || '';
    document.getElementById('editBirth').value = birth || '';
    document.getElementById('editRole').value = role || 'user';

    const g = gender || 'MALE';
    const genderSelect = document.getElementById('editGender');
    if (genderSelect) genderSelect.value = g;

    const cityInput = document.getElementById('editCity');
    if (cityInput) cityInput.value = city || '';

    document.getElementById('editForm').action = '/users/admin/update/' + id;

    const modal = document.getElementById('editModal');
    modal.style.display = 'block';
    modal.setAttribute('aria-hidden', 'false');
}

function closeEditModal() {
    const modal = document.getElementById('editModal');
    modal.style.display = 'none';
    modal.setAttribute('aria-hidden', 'true');
}

function openEditModalFromData(btn) {
    openEditModal(
        btn.getAttribute('data-id'),
        btn.getAttribute('data-name'),
        btn.getAttribute('data-email'),
        btn.getAttribute('data-birth'),
        btn.getAttribute('data-role'),
        btn.getAttribute('data-gender'),
        btn.getAttribute('data-city')
    );
}

function confirmDelete(form) {
    const name = form.getAttribute('data-name') || '';
    return confirm('Удалить пользователя "' + name + '"?');
}

window.addEventListener('click', function (event) {
    const modal = document.getElementById('editModal');
    if (event.target === modal) closeEditModal();
});

document.addEventListener('keydown', function (event) {
    if (event.key === 'Escape') closeEditModal();
});