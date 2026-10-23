function CalendarModule(config, container, moduleRef) {
    const styles = `
        .calendar-module { padding: 0.5rem; display: flex; flex-direction: column; height: 100%; }
        .calendar-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 0.5rem; }
        .calendar-header h4 { margin: 0; font-size: 1rem; color: var(--text-primary); }
        .calendar-nav { display: flex; gap: 0.25rem; }
        .calendar-weekdays { display: grid; grid-template-columns: repeat(7, 1fr); gap: 2px; margin-bottom: 2px; }
        .calendar-weekday { text-align: center; font-size: 0.65rem; font-weight: 600; color: var(--text-secondary); padding: 0.15rem 0; }
        .calendar-grid { display: grid; grid-template-columns: repeat(7, 1fr); grid-auto-rows: 1fr; gap: 2px; flex: 1; min-height: 180px; }
        .calendar-cell { background: var(--bg-tertiary); border-radius: 4px; padding: 2px; min-height: 34px; overflow: hidden; cursor: pointer; transition: background 0.15s; }
        .calendar-cell:hover { background: var(--bg-hover); }
        .calendar-cell.is-today { outline: 2px solid var(--primary-color); }
        .calendar-cell.is-empty { background: transparent; cursor: default; }
        .calendar-cell.is-empty:hover { background: transparent; }
        .cal-day-num { font-size: 0.65rem; color: var(--text-secondary); text-align: right; padding-right: 3px; }
        .calendar-cell.is-today .cal-day-num { color: var(--primary-color); font-weight: 700; }
        .cal-event { font-size: 0.6rem; padding: 1px 3px; border-radius: 3px; background: var(--primary-color); color: var(--text-on-primary); margin-top: 1px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; cursor: pointer; }
        .cal-event.allday { background: var(--success-color, #27ae60); }
        .cal-event.busy { background: var(--text-muted, #999); opacity: 0.55; cursor: default; }
        .calendar-footer { display: flex; justify-content: space-between; align-items: center; margin-top: 0.5rem; gap: 0.5rem; }
        .cal-filter-select { font-size: 0.75rem; padding: 0.25rem 0.4rem; border: 1px solid var(--input-border); border-radius: var(--radius); background: var(--input-bg); color: var(--text-primary); }
        .cal-modal-overlay { position: fixed; inset: 0; background: rgba(0,0,0,0.5); display: none; align-items: center; justify-content: center; z-index: 1000; }
        .cal-modal-overlay.show { display: flex; }
        .cal-modal { background: var(--bg-secondary, var(--bg-primary)); border-radius: var(--radius); padding: 1.25rem; width: 90%; max-width: 380px; max-height: 85vh; overflow-y: auto; color: var(--text-primary); }
        .cal-modal h4 { margin: 0 0 0.75rem; }
        .cal-modal .form-group { margin-bottom: 0.75rem; }
        .cal-modal label { display: block; font-size: 0.8125rem; margin-bottom: 0.25rem; color: var(--text-secondary); }
        .cal-modal input[type="text"], .cal-modal input[type="datetime-local"], .cal-modal textarea { width: 100%; padding: 0.4rem 0.5rem; border: 1px solid var(--input-border); border-radius: var(--radius); background: var(--input-bg); color: var(--text-primary); font-family: inherit; font-size: 0.8125rem; box-sizing: border-box; }
        .cal-modal input:focus, .cal-modal textarea:focus { outline: none; border-color: var(--primary-color); }
        .cal-modal .all-day-row { display: flex; align-items: center; gap: 0.4rem; font-size: 0.8125rem; color: var(--text-primary); cursor: pointer; user-select: none; }
        .cal-modal .all-day-row input { width: auto; cursor: pointer; }
        .cal-modal-actions { display: flex; gap: 0.5rem; justify-content: flex-end; margin-top: 1rem; }
        .cal-error { color: var(--danger-color); font-size: 0.8125rem; margin-top: 0.5rem; min-height: 1em; }
        .calendar-loading { text-align: center; color: var(--text-muted); padding: 1.5rem; font-size: 0.875rem; }
    `;

    container.innerHTML = `
        <div class="calendar-module">
            <style>${styles}</style>
            <div class="calendar-header">
                <div class="calendar-nav">
                    <button id="cal-prev" class="btn btn-secondary btn-sm" title="Previous month">&lsaquo;</button>
                    <button id="cal-next" class="btn btn-secondary btn-sm" title="Next month">&rsaquo;</button>
                </div>
                <h4 id="cal-title"></h4>
                <div class="calendar-nav">
                    <button id="cal-add" class="btn btn-primary btn-sm">+ Add</button>
                </div>
            </div>
            <div class="calendar-weekdays" id="cal-weekdays"></div>
            <div class="calendar-grid" id="cal-grid">
                <div class="calendar-loading">Loading events...</div>
            </div>
            <div class="calendar-footer">
                <select id="cal-filter" class="cal-filter-select" title="Filter displayed events">
                    <option value="all">All</option>
                    <option value="mine">My events</option>
                    <option value="shared">Shared with me</option>
                </select>
            </div>
            <div class="cal-modal-overlay" id="cal-modal-overlay">
                <div class="cal-modal">
                    <h4 id="cal-modal-title">Add Event</h4>
                    <div class="form-group">
                        <label for="cal-ev-title">Title</label>
                        <input type="text" id="cal-ev-title" maxlength="100" required>
                    </div>
                    <div class="form-group">
                        <label for="cal-ev-location">Location</label>
                        <input type="text" id="cal-ev-location" maxlength="200">
                    </div>
                    <div class="form-group">
                        <label class="all-day-row">
                            <input type="checkbox" id="cal-ev-allday">
                            All day
                        </label>
                    </div>
                    <div class="form-group">
                        <label for="cal-ev-start">Start</label>
                        <input type="datetime-local" id="cal-ev-start" required>
                    </div>
                    <div class="form-group">
                        <label for="cal-ev-end">End</label>
                        <input type="datetime-local" id="cal-ev-end" required>
                    </div>
                    <div class="form-group">
                        <label for="cal-ev-desc">Description</label>
                        <textarea id="cal-ev-desc" rows="3" maxlength="2000"></textarea>
                    </div>
                    <div class="cal-error" id="cal-error"></div>
                    <div class="cal-modal-actions">
                        <button id="cal-ev-delete" class="btn btn-danger btn-sm" style="margin-right: auto; display: none;">Delete</button>
                        <button id="cal-ev-cancel" class="btn btn-secondary btn-sm">Cancel</button>
                        <button id="cal-ev-save" class="btn btn-primary btn-sm">Save</button>
                    </div>
                </div>
            </div>
        </div>
    `;

    const grid = container.querySelector('#cal-grid');
    const weekdaysEl = container.querySelector('#cal-weekdays');
    const titleEl = container.querySelector('#cal-title');
    const overlay = container.querySelector('#cal-modal-overlay');

    // The modal must live outside the module card: a fixed-position element
    // inside an ancestor with a CSS transform (e.g. the card hover effect) is
    // positioned relative to that ancestor instead of the viewport.
    document.body.appendChild(overlay);
    const errorEl = overlay.querySelector('#cal-error');
    const deleteBtn = overlay.querySelector('#cal-ev-delete');

    const MONTH_NAMES = ['January', 'February', 'March', 'April', 'May', 'June', 'July', 'August', 'September', 'October', 'November', 'December'];
    const WEEKDAYS = ['Mo', 'Tu', 'We', 'Th', 'Fr', 'Sa', 'Su'];

    const today = new Date();
    let viewYear = today.getFullYear();
    let viewMonth = today.getMonth(); // 0-based

    let events = [];
    let editingId = null;
    let calendarFilter = config && config.filter ? config.filter : 'all';

    function matchesCalendarFilter(ev) {
        if (calendarFilter === 'mine') return !ev.ownerUsername;
        if (calendarFilter === 'shared') return !!ev.ownerUsername;
        return true;
    }

    function saveFilterToServer(value) {
        if (!moduleRef || !moduleRef.id) return;
        const updatedConfig = { ...(moduleRef.config || {}), filter: value };
        fetch(`${API_BASE}/modules/${moduleRef.id}`, {
            method: 'PUT',
            headers: { ...authHeaders(), 'Content-Type': 'application/json' },
            body: JSON.stringify({
                type: moduleRef.type,
                config: updatedConfig,
                enabled: moduleRef.enabled,
                displayOrder: moduleRef.displayOrder
            })
        }).then(response => {
            if (response.ok) {
                moduleRef.config = updatedConfig;
            } else {
                console.warn('Failed to persist calendar filter (HTTP ' + response.status + ')');
            }
        }).catch(e => console.warn('Failed to persist calendar filter:', e));
    }

    const authHeaders = () => ({ 'Authorization': `Bearer ${localStorage.getItem('authToken')}` });
    const dateKey = (y, m, d) => `${y}-${String(m + 1).padStart(2, '0')}-${String(d).padStart(2, '0')}`;

    function escapeHtml(text) {
        const div = document.createElement('div');
        div.textContent = text == null ? '' : String(text);
        return div.innerHTML;
    }

    function pad2(n) { return String(n).padStart(2, '0'); }

    function daysInMonth(y, m) { return new Date(y, m + 1, 0).getDate(); }
    function firstWeekday(y, m) { return (new Date(y, m, 1).getDay() + 6) % 7; } // Monday = 0

    function eventStartDay(ev, y, m) {
        const d = new Date(ev.startAt);
        return d.getFullYear() === y && d.getMonth() === m;
    }

    async function loadEvents() {
        try {
            const response = await fetch(`${API_BASE}/events/calendar`, { headers: authHeaders() });
            if (!response.ok) throw new Error(`HTTP ${response.status}`);
            events = await response.json();
            render();
        } catch (e) {
            grid.innerHTML = `<div class="calendar-loading">Could not load events (${escapeHtml(e.message)}).</div>`;
        }
    }

    function render() {
        titleEl.textContent = `${MONTH_NAMES[viewMonth]} ${viewYear}`;

        weekdaysEl.innerHTML = WEEKDAYS.map(w => `<div class="calendar-weekday">${w}</div>`).join('');

        grid.innerHTML = '';

        const total = daysInMonth(viewYear, viewMonth);
        const offset = firstWeekday(viewYear, viewMonth) + total;
        // Enough rows to fit; keep grid tiles even (6 rows max)
        const cells = Math.ceil(offset / 7) * 7;

        for (let i = 0; i < cells; i++) {
            if (i < firstWeekday(viewYear, viewMonth)) {
                grid.appendChild(emptyCell());
                continue;
            }
            const dayNum = i - firstWeekday(viewYear, viewMonth) + 1;
            grid.appendChild(dayCell(dayNum));
        }
    }

    function emptyCell() {
        const div = document.createElement('div');
        div.className = 'calendar-cell is-empty';
        return div;
    }

    function dayCell(dayNum) {
        const div = document.createElement('div');
        div.className = 'calendar-cell';

        const isToday = viewYear === today.getFullYear() && viewMonth === today.getMonth() && dayNum === today.getDate();
        if (isToday) div.classList.add('is-today');

        const key = dateKey(viewYear, viewMonth, dayNum);

        div.innerHTML = `<div class="cal-day-num">${dayNum}</div>`;
        events
            .filter(ev => matchesCalendarFilter(ev))
            .filter(ev => ev.startAt.slice(0, 10) === key)
            .forEach(ev => {
                if (ev.masked) {
                    const pill = document.createElement('div');
                    pill.className = 'cal-event busy';
                    pill.textContent = 'Busy';
                    pill.title = 'Busy (unshared event)';
                    pill.addEventListener('click', (e) => e.stopPropagation());
                    div.appendChild(pill);
                    return;
                }
                if (ev.ownerUsername) {
                    const pill = document.createElement('div');
                    pill.className = 'cal-event' + (ev.allDay ? ' allday' : '');
                    pill.textContent = ev.title;
                    pill.title = `${ev.title} (shared by @${ev.ownerUsername})`;
                    pill.addEventListener('click', (e) => e.stopPropagation());
                    div.appendChild(pill);
                    return;
                }
                const pill = document.createElement('div');
                pill.className = 'cal-event' + (ev.allDay ? ' allday' : '');
                pill.textContent = ev.title;
                if (ev.location) pill.title = `${ev.title} @ ${ev.location}`;
                pill.addEventListener('click', (e) => {
                    e.stopPropagation();
                    openModal(ev);
                });
                div.appendChild(pill);
            });

        div.addEventListener('click', () => openModal(null, key));
        return div;
    }

    function openModal(ev, dateStr) {
        editingId = ev ? ev.id : null;
        overlay.querySelector('#cal-modal-title').textContent = ev ? 'Edit Event' : 'Add Event';
        errorEl.textContent = '';

        const startDate = ev ? ev.startAt.slice(0, 10) : dateStr;
        const startDateTime = startDate + 'T' + (ev && !ev.allDay ? ev.startAt.slice(11, 16) : '09:00');
        const endDateTime = startDate + 'T' + (ev && !ev.allDay ? ev.endAt.slice(11, 16) : '10:00');

        overlay.querySelector('#cal-ev-title').value = ev ? ev.title : '';
        overlay.querySelector('#cal-ev-location').value = ev ? (ev.location || '') : '';
        overlay.querySelector('#cal-ev-desc').value = ev ? (ev.description || '') : '';
        overlay.querySelector('#cal-ev-allday').checked = ev ? ev.allDay : false;
        overlay.querySelector('#cal-ev-start').value = startDateTime;
        overlay.querySelector('#cal-ev-end').value = endDateTime;

        deleteBtn.style.display = ev ? '' : 'none';
        overlay.classList.add('show');
    }

    function closeModal() {
        overlay.classList.remove('show');
        editingId = null;
    }

    async function saveEvent() {
        const title = overlay.querySelector('#cal-ev-title').value.trim();
        const allDay = overlay.querySelector('#cal-ev-allday').checked;
        const startInput = overlay.querySelector('#cal-ev-start').value;
        const endInput = overlay.querySelector('#cal-ev-end').value;

        errorEl.textContent = '';
        if (!title) { errorEl.textContent = 'Title is required.'; return; }
        if (!startInput || !endInput) { errorEl.textContent = 'Start and end are required.'; return; }

        const payload = {
            title,
            description: overlay.querySelector('#cal-ev-desc').value.trim() || null,
            location: overlay.querySelector('#cal-ev-location').value.trim() || null,
            allDay,
            startAt: allDay ? startInput.slice(0, 10) + 'T00:00' : startInput,
            endAt: allDay ? endInput.slice(0, 10) + 'T23:59' : endInput
        };

        try {
            const url = editingId ? `${API_BASE}/events/calendar/${editingId}` : `${API_BASE}/events/calendar`;
            const method = editingId ? 'PUT' : 'POST';
            const response = await fetch(url, {
                method,
                headers: { ...authHeaders(), 'Content-Type': 'application/json' },
                body: JSON.stringify(payload)
            });

            if (!response.ok) {
                const data = await response.json().catch(() => ({}));
                throw new Error(data.message || `HTTP ${response.status}`);
            }

            closeModal();
            document.dispatchEvent(new CustomEvent('calendar:updated'));
        } catch (e) {
            errorEl.textContent = e.message;
        }
    }

    async function deleteEvent() {
        if (!editingId) return;
        try {
            const response = await fetch(`${API_BASE}/events/calendar/${editingId}`, {
                method: 'DELETE',
                headers: authHeaders()
            });
            if (!response.ok) {
                const data = await response.json().catch(() => ({}));
                throw new Error(data.message || `HTTP ${response.status}`);
            }
            closeModal();
            document.dispatchEvent(new CustomEvent('calendar:updated'));
        } catch (e) {
            errorEl.textContent = e.message;
        }
    }

    // Wire up UI
    container.querySelector('#cal-prev').addEventListener('click', () => {
        viewMonth--;
        if (viewMonth < 0) { viewMonth = 11; viewYear--; }
        render();
    });

    container.querySelector('#cal-next').addEventListener('click', () => {
        viewMonth++;
        if (viewMonth > 11) { viewMonth = 0; viewYear++; }
        render();
    });

    container.querySelector('#cal-add').addEventListener('click', () => openModal(null, dateKey(viewYear, viewMonth, today.getDate())));

    const filterSelect = container.querySelector('#cal-filter');
    filterSelect.value = calendarFilter;
    filterSelect.addEventListener('change', () => {
        calendarFilter = filterSelect.value;
        saveFilterToServer(calendarFilter);
        render();
    });
    deleteBtn.addEventListener('click', deleteEvent);
    overlay.querySelector('#cal-ev-save').addEventListener('click', saveEvent);
    overlay.querySelector('#cal-ev-cancel').addEventListener('click', closeModal);

    overlay.addEventListener('click', (e) => {
        if (e.target === overlay) closeModal();
    });

    // Keep every calendar module on this page in sync after data changes
    document.addEventListener('calendar:updated', () => loadEvents());

    loadEvents();
}

if (typeof window !== 'undefined') {
    window.CalendarModule = CalendarModule;
}