function previewImage(event) {
    const input = event.target;
    const preview = document.getElementById('previewImage');
    const previewContainer = document.getElementById('imagePreview');

    if (input.files && input.files[0]) {
        const reader = new FileReader();
        reader.onload = function(e) {
            preview.src = e.target.result;
            previewContainer.style.display = 'flex';
        };
        reader.readAsDataURL(input.files[0]);
    } else {
        preview.src = '';
        previewContainer.style.display = 'none';
    }
}
