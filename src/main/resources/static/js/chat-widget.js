/* BankSystem Chat Widget — лёгкий виджет как у банков.
 * Работает с /api/chat (Bearer token если есть), иначе будет fallback на локальные ответы.
 */

(function () {
  const STORAGE_KEY = 'bank_chat_history_v1';
  const API_URL = '/api/chat';

  function token() {
    return localStorage.getItem('token');
  }

  function authHeaders() {
    const h = { 'Content-Type': 'application/json' };
    const t = token();
    if (t) h['Authorization'] = 'Bearer ' + t;
    return h;
  }

  function nowTime() {
    const d = new Date();
    return d.toLocaleTimeString('ru-RU', { hour: '2-digit', minute: '2-digit' });
  }

  function loadHistory() {
    try {
      const raw = localStorage.getItem(STORAGE_KEY);
      return raw ? JSON.parse(raw) : [];
    } catch {
      return [];
    }
  }

  function saveHistory(items) {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(items.slice(-100)));
  }

  function escapeHtml(s) {
    return (s || '').replace(/[&<>"']/g, (c) => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]));
  }

  function localFallbackAnswer(text) {
    const msg = (text || '').trim().toLowerCase();
    if (!msg) return { reply: 'Напишите вопрос — я помогу.' };
    if (msg.includes('привет') || msg.includes('здрав') || msg.includes('сәлем') || msg.includes('салам')) {
      return { reply: 'Здравствуйте! Я помощник BankSystem. Чем могу помочь?', quickReplies: ['Мои счета', 'Перевод', 'Кредиты', 'Вклады'] };
    }
    if (msg.includes('счет') || msg.includes('счёт') || msg.includes('баланс')) {
      return { reply: 'Счета — в разделе «Мои счета». Там баланс и валюта. Хотите открыть новый счёт?', quickReplies: ['Открыть счёт', 'История операций'] };
    }
    if (msg.includes('перевод')) {
      return { reply: 'Перевод: «Переводы» → счёт отправителя → номер получателя → сумма → «Отправить».', quickReplies: ['Комиссия', 'Лимиты', 'История'] };
    }
    if (msg.includes('кредит')) {
      return { reply: 'Кредиты — в разделе «Кредиты». Можно подать заявку, админ обработает.', quickReplies: ['Открыть кредиты', 'Статусы'] };
    }
    if (msg.includes('вклад') || msg.includes('депозит')) {
      return { reply: 'Вклады — в разделе «Вклады». Можно подать заявку, админ обработает.', quickReplies: ['Открыть вклады', 'Проценты'] };
    }
    if (msg.includes('пароль') || msg.includes('забыл') || msg.includes('unlock') || msg.includes('сброс')) {
      return { reply: 'Нажмите «Забыли пароль?» на странице входа — мы отправим код на email.', quickReplies: ['Открыть восстановление'] };
    }
    return { reply: 'Я пока отвечаю на базовые вопросы по банкингу. Напишите: счета / перевод / кредиты / вклады / пароль.' };
  }

  // Build UI
  const root = document.createElement('div');
  root.id = 'bankChatWidget';
  root.innerHTML = `
    <button class="bcw-fab" aria-label="Открыть чат" title="Чат с помощником">
      <span class="bcw-fab-icon">💬</span>
      <span class="bcw-fab-dot" style="display:none"></span>
    </button>

    <div class="bcw-panel" aria-hidden="true">
      <div class="bcw-header">
        <div class="bcw-title">
          <div class="bcw-avatar">🏦</div>
          <div>
            <div class="bcw-name">Помощник BankSystem</div>
            <div class="bcw-sub">Онлайн • отвечаю за 5–10 сек</div>
          </div>
        </div>
        <div class="bcw-actions">
          <button class="bcw-iconbtn" data-action="clear" title="Очистить историю">🧹</button>
          <button class="bcw-iconbtn" data-action="close" title="Закрыть">✕</button>
        </div>
      </div>

      <div class="bcw-body">
        <div class="bcw-msgs"></div>
      </div>

      <div class="bcw-quick" style="display:none"></div>

      <div class="bcw-footer">
        <input class="bcw-input" type="text" placeholder="Напишите сообщение…" maxlength="500" />
        <button class="bcw-send" title="Отправить">➤</button>
      </div>

      <div class="bcw-note">Не сообщайте пароли и коды. Для тестов используйте безопасные данные.</div>
    </div>
  `;

  const style = document.createElement('style');
  style.textContent = `
    #bankChatWidget{position:fixed;right:18px;bottom:18px;z-index:9999;font-family:Segoe UI,system-ui,-apple-system,sans-serif}
    #bankChatWidget *{box-sizing:border-box}

    .bcw-fab{width:56px;height:56px;border-radius:18px;border:1px solid rgba(255,255,255,.12);background:linear-gradient(135deg,#667eea,#764ba2);color:#fff;cursor:pointer;box-shadow:0 14px 35px rgba(0,0,0,.45);display:flex;align-items:center;justify-content:center;position:relative;transition:transform .2s ease,box-shadow .2s ease}
    .bcw-fab:hover{transform:translateY(-2px);box-shadow:0 18px 45px rgba(0,0,0,.55)}
    .bcw-fab-icon{font-size:20px}
    .bcw-fab-dot{position:absolute;top:10px;right:10px;width:9px;height:9px;border-radius:50%;background:#4ade80;box-shadow:0 0 0 4px rgba(74,222,128,.18)}

    .bcw-panel{width:360px;max-width:calc(100vw - 36px);height:520px;max-height:calc(100vh - 120px);background:rgba(13,13,26,.98);border:1px solid rgba(255,255,255,.10);border-radius:18px;overflow:hidden;box-shadow:0 25px 70px rgba(0,0,0,.65);display:none;flex-direction:column}
    .bcw-panel.open{display:flex;animation:bcwIn .18s ease-out}
    @keyframes bcwIn{from{transform:translateY(8px);opacity:.0}to{transform:translateY(0);opacity:1}}

    .bcw-header{padding:14px 14px 12px;border-bottom:1px solid rgba(255,255,255,.08);display:flex;align-items:center;justify-content:space-between;gap:12px}
    .bcw-title{display:flex;align-items:center;gap:10px;min-width:0}
    .bcw-avatar{width:38px;height:38px;border-radius:12px;background:rgba(255,255,255,.06);display:flex;align-items:center;justify-content:center;font-size:18px;border:1px solid rgba(255,255,255,.10)}
    .bcw-name{color:#fff;font-weight:800;font-size:14px;white-space:nowrap;overflow:hidden;text-overflow:ellipsis}
    .bcw-sub{color:rgba(255,255,255,.45);font-size:12px}
    .bcw-actions{display:flex;gap:8px}
    .bcw-iconbtn{width:32px;height:32px;border-radius:10px;border:1px solid rgba(255,255,255,.10);background:rgba(255,255,255,.06);color:#fff;cursor:pointer}
    .bcw-iconbtn:hover{background:rgba(255,255,255,.10)}

    .bcw-body{flex:1;padding:14px;overflow:auto}
    .bcw-msgs{display:flex;flex-direction:column;gap:10px}

    .bcw-msg{display:flex;gap:10px;align-items:flex-end}
    .bcw-msg.you{justify-content:flex-end}
    .bcw-bubble{max-width:78%;padding:10px 12px;border-radius:14px;border:1px solid rgba(255,255,255,.10);background:rgba(255,255,255,.06);color:rgba(255,255,255,.92);font-size:13px;line-height:1.35;white-space:pre-wrap}
    .bcw-msg.you .bcw-bubble{background:linear-gradient(135deg,rgba(102,126,234,.32),rgba(118,75,162,.20));border-color:rgba(102,126,234,.25)}
    .bcw-time{font-size:11px;color:rgba(255,255,255,.35);margin:0 2px}

    .bcw-typing{display:inline-flex;gap:4px;align-items:center}
    .bcw-typing span{width:5px;height:5px;border-radius:50%;background:rgba(255,255,255,.55);display:inline-block;animation:bcwDot 1s infinite}
    .bcw-typing span:nth-child(2){animation-delay:.15s}
    .bcw-typing span:nth-child(3){animation-delay:.3s}
    @keyframes bcwDot{0%,100%{opacity:.25;transform:translateY(0)}50%{opacity:1;transform:translateY(-2px)}}

    .bcw-quick{padding:10px 12px;border-top:1px solid rgba(255,255,255,.08);display:flex;flex-wrap:wrap;gap:8px}
    .bcw-quick button{padding:7px 10px;border-radius:999px;border:1px solid rgba(255,255,255,.10);background:rgba(255,255,255,.06);color:#fff;font-size:12px;font-weight:650;cursor:pointer}
    .bcw-quick button:hover{background:rgba(255,255,255,.10)}

    .bcw-footer{padding:12px;border-top:1px solid rgba(255,255,255,.08);display:flex;gap:10px}
    .bcw-input{flex:1;border-radius:12px;border:1px solid rgba(255,255,255,.12);background:rgba(255,255,255,.06);color:#fff;padding:10px 12px;font-size:13px;outline:none}
    .bcw-input:focus{border-color:rgba(102,126,234,.55);background:rgba(102,126,234,.10)}
    .bcw-send{width:42px;border-radius:12px;border:0;background:linear-gradient(135deg,#667eea,#764ba2);color:#fff;cursor:pointer;font-weight:900}
    .bcw-send:hover{filter:brightness(1.05)}

    .bcw-note{padding:10px 12px;color:rgba(255,255,255,.35);font-size:11px}

    @media(max-width:480px){
      .bcw-panel{width:calc(100vw - 36px);height:70vh}
    }
  `;

  document.body.appendChild(style);
  document.body.appendChild(root);

  const fab = root.querySelector('.bcw-fab');
  const panel = root.querySelector('.bcw-panel');
  const msgs = root.querySelector('.bcw-msgs');
  const input = root.querySelector('.bcw-input');
  const sendBtn = root.querySelector('.bcw-send');
  const quick = root.querySelector('.bcw-quick');
  const dot = root.querySelector('.bcw-fab-dot');

  function scrollBottom() {
    const body = root.querySelector('.bcw-body');
    body.scrollTop = body.scrollHeight;
  }

  function addMessage(who, text, time) {
    const t = time || nowTime();
    const div = document.createElement('div');
    div.className = 'bcw-msg ' + (who === 'you' ? 'you' : 'bot');
    div.innerHTML = `
      <div class="bcw-bubble">${escapeHtml(text)}</div>
      <div class="bcw-time">${escapeHtml(t)}</div>
    `;
    msgs.appendChild(div);
    scrollBottom();
  }

  function setQuickReplies(items) {
    if (!items || !items.length) {
      quick.style.display = 'none';
      quick.innerHTML = '';
      return;
    }
    quick.style.display = 'flex';
    quick.innerHTML = items.map(s => `<button type="button">${escapeHtml(s)}</button>`).join('');
    quick.querySelectorAll('button').forEach(btn => {
      btn.addEventListener('click', () => {
        input.value = btn.textContent;
        input.focus();
        send();
      });
    });
  }

  function showTyping() {
    const div = document.createElement('div');
    div.className = 'bcw-msg bot';
    div.dataset.typing = '1';
    div.innerHTML = `
      <div class="bcw-bubble"><span class="bcw-typing"><span></span><span></span><span></span></span></div>
      <div class="bcw-time">${nowTime()}</div>
    `;
    msgs.appendChild(div);
    scrollBottom();
  }

  function hideTyping() {
    const t = msgs.querySelector('[data-typing="1"]');
    if (t) t.remove();
  }

  async function askServer(message) {
    const res = await fetch(API_URL, { method: 'POST', headers: authHeaders(), body: JSON.stringify({ message }) });
    if (!res.ok) throw new Error('HTTP ' + res.status);
    return await res.json();
  }

  async function send() {
    const text = (input.value || '').trim();
    if (!text) return;

    input.value = '';
    addMessage('you', text);

    const history = loadHistory();
    history.push({ who: 'you', text, time: nowTime() });
    saveHistory(history);

    setQuickReplies(null);
    showTyping();

    try {
      const data = await askServer(text);
      hideTyping();
      const reply = data && data.reply ? data.reply : 'Я не смог сформировать ответ.';
      addMessage('bot', reply);
      const history2 = loadHistory();
      history2.push({ who: 'bot', text: reply, time: nowTime() });
      saveHistory(history2);
      setQuickReplies(data.quickReplies);
    } catch (e) {
      // fallback
      hideTyping();
      const fb = localFallbackAnswer(text);
      addMessage('bot', fb.reply);
      const history2 = loadHistory();
      history2.push({ who: 'bot', text: fb.reply, time: nowTime() });
      saveHistory(history2);
      setQuickReplies(fb.quickReplies);
    }
  }

  function open() {
    panel.classList.add('open');
    panel.setAttribute('aria-hidden', 'false');
    dot.style.display = 'none';
    input.focus();
    scrollBottom();
  }

  function close() {
    panel.classList.remove('open');
    panel.setAttribute('aria-hidden', 'true');
  }

  function toggle() {
    const isOpen = panel.classList.contains('open');
    if (isOpen) close(); else open();
  }

  function clearHistory() {
    localStorage.removeItem(STORAGE_KEY);
    msgs.innerHTML = '';
    bootGreeting(true);
  }

  function bootGreeting(force) {
    const history = loadHistory();
    if (!force && history.length) {
      history.forEach(m => addMessage(m.who, m.text, m.time));
      setQuickReplies(['Мои счета', 'Перевод', 'Кредиты', 'Вклады']);
      return;
    }
    addMessage('bot', 'Здравствуйте! Я помощник BankSystem. Могу подсказать по счетам, переводам, кредитам и восстановлению доступа.');
    setQuickReplies(['Мои счета', 'Перевод', 'Кредиты', 'Забыли пароль']);
  }

  fab.addEventListener('click', toggle);
  sendBtn.addEventListener('click', send);
  input.addEventListener('keydown', (e) => {
    if (e.key === 'Enter') {
      e.preventDefault();
      send();
    }
  });

  root.querySelectorAll('.bcw-iconbtn').forEach(btn => {
    btn.addEventListener('click', () => {
      const act = btn.getAttribute('data-action');
      if (act === 'close') close();
      if (act === 'clear') clearHistory();
    });
  });

  // New message dot if closed
  const originalAddMessage = addMessage;
  addMessage = function (who, text, time) {
    originalAddMessage(who, text, time);
    if (who === 'bot' && !panel.classList.contains('open')) {
      dot.style.display = 'block';
    }
  };

  // initial
  bootGreeting(false);
})();

