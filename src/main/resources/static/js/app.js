// ============================================
// STOCKWISE - Main JavaScript
// ============================================

document.addEventListener('DOMContentLoaded', function () {

  document.querySelectorAll('.alert-dismissible').forEach(function (alert) {
    setTimeout(function () {
      alert.style.opacity = '0';
      alert.style.transform = 'translateY(-8px)';
      alert.style.transition = 'all 0.3s ease';
      setTimeout(function () { alert.remove(); }, 300);
    }, 5000);
  });

  document.querySelectorAll('.alert-close').forEach(function (btn) {
    btn.addEventListener('click', function () {
      var alert = btn.closest('.alert');
      alert.style.opacity = '0';
      alert.style.transition = 'opacity 0.2s';
      setTimeout(function () { alert.remove(); }, 200);
    });
  });

});
