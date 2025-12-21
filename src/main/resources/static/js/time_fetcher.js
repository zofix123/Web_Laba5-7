// Простой рабочий вариант живых часов
function startLiveClock() {
    const timeElement = document.getElementById('serverTime');
    if (!timeElement) {
        console.log('Элемент serverTime не найден');
        return;
    }

    // Получаем начальное время из элемента
    const serverTimeText = timeElement.textContent.trim();
    console.log('Начальное время:', serverTimeText);

    // Парсим время с сервера или используем текущее
    let currentTime;

    if (serverTimeText && serverTimeText.includes('.')) {
        try {
            // Формат: "21.12.2025 23:45:30"
            const [dateStr, timeStr] = serverTimeText.split(' ');
            const [day, month, year] = dateStr.split('.');
            const [hours, minutes, seconds] = timeStr.split(':');

            currentTime = new Date(
                parseInt(year),
                parseInt(month) - 1,
                parseInt(day),
                parseInt(hours),
                parseInt(minutes),
                parseInt(seconds || 0)
            );

            if (isNaN(currentTime.getTime())) {
                throw new Error('Invalid date');
            }
        } catch (e) {
            console.warn('Не удалось распарсить, использую текущее время:', e);
            currentTime = new Date();
        }
    } else {
        currentTime = new Date();
    }

    // Функция обновления времени
    function updateClock() {
        currentTime.setSeconds(currentTime.getSeconds() + 1);

        // Форматируем в русский формат
        const formattedTime =
            currentTime.getDate().toString().padStart(2, '0') + '.' +
            (currentTime.getMonth() + 1).toString().padStart(2, '0') + '.' +
            currentTime.getFullYear() + ' ' +
            currentTime.getHours().toString().padStart(2, '0') + ':' +
            currentTime.getMinutes().toString().padStart(2, '0') + ':' +
            currentTime.getSeconds().toString().padStart(2, '0');

        timeElement.textContent = formattedTime;
    }

    // Сначала обновляем
    updateClock();

    // Затем каждую секунду
    setInterval(updateClock, 1000);

    console.log('Часы запущены');
}

// Запускаем при загрузке страницы
document.addEventListener('DOMContentLoaded', startLiveClock);