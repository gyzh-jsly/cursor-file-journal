(() => {
    const state = {
        currentYear: new Date().getFullYear(),
        currentMonth: new Date().getMonth() + 1,
        yearStats: {},
        monthData: {},
        initialized: false,
        isMonthView: false
    };

    function getEl(id) {
        return document.getElementById(id);
    }

    function populateYearSelects(baseYear) {
        const start = baseYear - 5;
        const end = baseYear + 5;
        const yearSelectForYearView = getEl('yearSelectForYearView');
        const yearSelectForMonthView = getEl('yearSelectForMonthView');

        yearSelectForYearView.innerHTML = '';
        yearSelectForMonthView.innerHTML = '';

        for (let y = start; y <= end; y++) {
            const yearLabel = y + '年';
            const optA = new Option(yearLabel, String(y));
            const optB = new Option(yearLabel, String(y));
            yearSelectForYearView.add(optA);
            yearSelectForMonthView.add(optB);
        }
        yearSelectForYearView.value = String(state.currentYear);
        yearSelectForMonthView.value = String(state.currentYear);
    }

    function syncSelectors() {
        getEl('yearSelectForYearView').value = String(state.currentYear);
        getEl('yearSelectForMonthView').value = String(state.currentYear);
        getEl('monthSelect').value = String(state.currentMonth);
    }

    async function loadYearStats() {
        const res = await fetch(`/diary/calendar/year-stats?year=${state.currentYear}`);
        if (!res.ok) throw new Error('加载全年数据失败');
        state.yearStats = await res.json();
        renderYearView();
    }

    async function loadMonthData() {
        const res = await fetch(`/diary/calendar/data?year=${state.currentYear}&month=${state.currentMonth}`);
        if (!res.ok) throw new Error('加载月历数据失败');
        state.monthData = await res.json();
        renderMonthView();
    }

    function renderYearView() {
        const monthLabels = ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月'];
        const html = [];

        for (let month = 1; month <= 12; month++) {
            const count = Number(state.yearStats[month] || 0);
            html.push(`
                <button type="button" class="year-month-card" data-month="${month}">
                    <div class="month-name">${monthLabels[month - 1]}</div>
                    <div class="diary-count">${count}篇日记</div>
                </button>
            `);
        }
        getEl('yearGrid').innerHTML = html.join('');

        getEl('yearGrid').querySelectorAll('.year-month-card').forEach((card) => {
            card.addEventListener('click', () => {
                switchToMonthView(Number(card.getAttribute('data-month')));
            });
        });
    }

    function renderMonthView() {
        const year = state.currentYear;
        const month = state.currentMonth;
        const firstDay = new Date(year, month - 1, 1);
        const lastDay = new Date(year, month, 0);
        const totalDays = lastDay.getDate();
        let startDay = firstDay.getDay();
        startDay = startDay === 0 ? 7 : startDay;

        const cells = [];
        for (let i = 1; i < startDay; i++) {
            cells.push('<div class="day-cell day-cell-empty"></div>');
        }

        for (let day = 1; day <= totalDays; day++) {
            const dateStr = `${year}-${String(month).padStart(2, '0')}-${String(day).padStart(2, '0')}`;
            const diaryCount = Number(state.monthData[dateStr] || 0);
            const hasDiary = diaryCount > 0;
            cells.push(`
                <button type="button" class="day-cell ${hasDiary ? 'has-diary' : 'no-diary'}" ${hasDiary ? `data-date="${dateStr}"` : 'disabled'}>
                    <span class="day-number">${day}</span>
                    ${hasDiary ? `<span class="day-badge">·${diaryCount}</span>` : ''}
                </button>
            `);
        }

        while (cells.length % 7 !== 0) {
            cells.push('<div class="day-cell day-cell-empty"></div>');
        }

        getEl('monthGrid').innerHTML = cells.join('');
        getEl('monthGrid').querySelectorAll('.day-cell.has-diary').forEach((cell) => {
            cell.addEventListener('click', () => {
                const dateStr = cell.getAttribute('data-date');
                if (window.parent && typeof window.parent.openDiaryListTab === 'function') {
                    window.parent.openDiaryListTab(dateStr);
                }
            });
        });
    }

    function switchToMonthView(month) {
        state.currentMonth = month;
        state.isMonthView = true;
        syncSelectors();
        getEl('yearView').style.display = 'none';
        getEl('monthView').style.display = 'flex';
        loadMonthData().catch(console.error);
    }

    function switchToYearView() {
        state.isMonthView = false;
        getEl('monthView').style.display = 'none';
        getEl('yearView').style.display = 'flex';
        syncSelectors();
        loadYearStats().catch(console.error);
    }

    function bindEventsOnce() {
        if (state.initialized) return;

        getEl('prevYearBtn').addEventListener('click', () => {
            state.currentYear -= 1;
            populateYearSelects(state.currentYear);
            loadYearStats().catch(console.error);
        });

        getEl('nextYearBtn').addEventListener('click', () => {
            state.currentYear += 1;
            populateYearSelects(state.currentYear);
            loadYearStats().catch(console.error);
        });

        getEl('yearSelectForYearView').addEventListener('change', (e) => {
            state.currentYear = Number(e.target.value);
            populateYearSelects(state.currentYear);
            loadYearStats().catch(console.error);
        });

        getEl('backToYearBtn').addEventListener('click', switchToYearView);

        getEl('prevMonthBtn').addEventListener('click', () => {
            if (state.currentMonth === 1) {
                state.currentYear -= 1;
                state.currentMonth = 12;
            } else {
                state.currentMonth -= 1;
            }
            populateYearSelects(state.currentYear);
            syncSelectors();
            loadMonthData().catch(console.error);
        });

        getEl('nextMonthBtn').addEventListener('click', () => {
            if (state.currentMonth === 12) {
                state.currentYear += 1;
                state.currentMonth = 1;
            } else {
                state.currentMonth += 1;
            }
            populateYearSelects(state.currentYear);
            syncSelectors();
            loadMonthData().catch(console.error);
        });

        getEl('yearSelectForMonthView').addEventListener('change', (e) => {
            state.currentYear = Number(e.target.value);
            populateYearSelects(state.currentYear);
            syncSelectors();
            loadMonthData().catch(console.error);
        });

        getEl('monthSelect').addEventListener('change', (e) => {
            state.currentMonth = Number(e.target.value);
            syncSelectors();
            loadMonthData().catch(console.error);
        });

        const openWriteTab = () => {
            if (window.parent && typeof window.parent.openWriteDiaryTab === 'function') {
                window.parent.openWriteDiaryTab();
            }
        };
        getEl('writeDiaryBtnFromYear').addEventListener('click', openWriteTab);
        getEl('writeDiaryBtnFromMonth').addEventListener('click', openWriteTab);

        state.initialized = true;
    }

    function initCalendar() {
        bindEventsOnce();
        state.currentYear = new Date().getFullYear();
        state.currentMonth = new Date().getMonth() + 1;
        state.isMonthView = false;
        populateYearSelects(state.currentYear);
        syncSelectors();
        getEl('monthView').style.display = 'none';
        getEl('yearView').style.display = 'block';
        loadYearStats().catch(console.error);
    }

    function refreshCurrentView() {
        if (state.isMonthView) {
            loadMonthData().catch(console.error);
        } else {
            loadYearStats().catch(console.error);
        }
    }

    window.initCalendar = initCalendar;
    window.refreshCalendarCurrentView = refreshCurrentView;

    document.addEventListener('DOMContentLoaded', () => {
        if (typeof window.initCalendar === 'function') {
            window.initCalendar();
        }
    });
})();



//write-diary.html的script
