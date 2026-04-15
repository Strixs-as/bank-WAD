(function () {
  /**
   * Единственный способ выхода из системы по всему фронту.
   * - Чистит локальные JWT-данные
   * - Делает best-effort POST /logout (если есть server-side session)
   * - Всегда редиректит на /index.html
   */
  async function doLogout(e) {
    if (e && typeof e.preventDefault === 'function') e.preventDefault();

    try {
      localStorage.removeItem('token');
      localStorage.removeItem('userName');
      localStorage.removeItem('userId');
      localStorage.removeItem('userEmail');
    } catch (err) {
      // ignore
    }

    try {
      await fetch('/logout', {
        method: 'POST',
        credentials: 'include',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: ''
      });
    } catch (err) {
      // ignore
    }

    window.location.href = '/index.html';
    return false;
  }

  window.bankLogout = doLogout;
})();

