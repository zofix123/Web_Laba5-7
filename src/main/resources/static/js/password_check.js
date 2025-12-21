function checkPasswordStrength() {
    // Ищем поле пароля по разным ID
    let passwordField = document.getElementById('newPassword') ||
        document.getElementById('password');
    let strengthDiv = document.getElementById('password-strength');

    if (!passwordField || !strengthDiv) return;

    const password = passwordField.value;

    if (password.length === 0) {
        strengthDiv.innerHTML = '';
        return;
    }

    if (password.length < 4) {
        strengthDiv.innerHTML = 'Слишком короткий пароль (минимум 4 символа)';
        strengthDiv.style.color = 'red';
    } else if (password.length < 6) {
        strengthDiv.innerHTML = 'Слабый пароль';
        strengthDiv.style.color = 'orange';
    } else if (password.length < 8) {
        strengthDiv.innerHTML = 'Средний пароль';
        strengthDiv.style.color = 'blue';
    } else {
        strengthDiv.innerHTML = 'Надежный пароль';
        strengthDiv.style.color = 'green';
    }
}

// Валидация пароля при отправке формы
function validatePassword() {
    // Ищем поля по разным ID для разных страниц
    let passwordField = document.getElementById('newPassword') ||
        document.getElementById('password');
    let confirmField = document.getElementById('confirmNewPassword') ||
        document.getElementById('confirmPassword');
    let matchDiv = document.getElementById('password-match');

    if (!passwordField || !confirmField || !matchDiv) {
        console.error('Не найдены элементы для валидации пароля');
        return false;
    }

    const password = passwordField.value;
    const confirmPassword = confirmField.value;

    if (password.length < 4) {
        matchDiv.innerHTML = 'Пароль должен содержать минимум 4 символа';
        matchDiv.style.color = 'red';
        return false;
    }

    if (password !== confirmPassword) {
        matchDiv.innerHTML = 'Пароли не совпадают';
        matchDiv.style.color = 'red';
        return false;
    } else {
        matchDiv.innerHTML = 'Пароли совпадают ✓';
        matchDiv.style.color = 'green';
    }

    return true;
}

// Реальное время проверки совпадения паролей
function setupPasswordValidation() {
    // Находим все поля подтверждения пароля на странице
    const confirmFields = [
        document.getElementById('confirmNewPassword'),
        document.getElementById('confirmPassword')
    ].filter(field => field !== null);

    // Добавляем обработчика
    confirmFields.forEach(confirmField => {
        confirmField.addEventListener('input', function() {
            // Находим соответствующее поле пароля
            let passwordField;
            if (this.id === 'confirmNewPassword') {
                passwordField = document.getElementById('newPassword');
            } else if (this.id === 'confirmPassword') {
                passwordField = document.getElementById('password');
            }

            const matchDiv = document.getElementById('password-match');

            if (!passwordField || !matchDiv) return;

            const password = passwordField.value;
            const confirmPassword = this.value;

            if (confirmPassword.length === 0) {
                matchDiv.innerHTML = '';
                return;
            }

            if (password !== confirmPassword) {
                matchDiv.innerHTML = 'Пароли не совпадают';
                matchDiv.style.color = 'red';
            } else {
                matchDiv.innerHTML = 'Пароли совпадают ✓';
                matchDiv.style.color = 'green';
            }
        });
    });

    // Обработчики для проверки сложности
    const passwordFields = [
        document.getElementById('newPassword'),
        document.getElementById('password')
    ].filter(field => field !== null);

    passwordFields.forEach(field => {
        field.addEventListener('input', checkPasswordStrength);
    });
}

// Инициализация при загрузке страницы
document.addEventListener('DOMContentLoaded', function() {
    console.log('Инициализация проверки паролей...');
    setupPasswordValidation();

    setTimeout(setupPasswordValidation, 100);
});