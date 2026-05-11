/*<index_1:*/
////标签页切换逻辑：
// 标签页管理
function openTab(name) {
    // 更新左侧导航高亮
    document.querySelectorAll('.nav-item').forEach(n => n.classList.remove('active'));
    event.target.classList.add('active');

    // 检查标签是否已存在
    let existing = document.querySelector(`.tab-item[data-tab="${name}"]`);
    if (existing) {
        switchTab(name);
        return;
    }

    // 创建新标签
    const labels = { home: '首页', monitor: '监控目录', manual: '手选文件', archive: '档案库', diary: '写日记', past: '往期存档' };
    const tabNav = document.getElementById('tabNav');
    const tab = document.createElement('span');
    tab.className = 'tab-item';
    tab.setAttribute('data-tab', name);
    tab.innerHTML = `${labels[name] || name} <span class="tab-close" onclick="closeTab(event, '${name}')">&times;</span>`;
    tab.onclick = function() { switchTab(name); };
    tabNav.appendChild(tab);

    // 创建内容区
    const pane = document.getElementById('pane-' + name);
    if (name === 'monitor') {
        loadMonitorPane(pane);
    } else if (name === 'diary') {
        loadDiaryPane(pane);
    }

    switchTab(name);
}

function switchTab(name) {
    document.querySelectorAll('.tab-item').forEach(t => t.classList.remove('active'));
    document.querySelector(`.tab-item[data-tab="${name}"]`)?.classList.add('active');
    document.querySelectorAll('.tab-pane').forEach(p => p.classList.remove('active'));
    document.getElementById('pane-' + name)?.classList.add('active');
}

function closeTab(event, name) {
    event.stopPropagation();
    // 移除标签
    const tab = document.querySelector(`.tab-item[data-tab="${name}"]`);
    if (tab) tab.remove();
    // 如果关闭的是当前激活的，切换到首页
    if (tab.classList.contains('active')) {
        switchTab('home');
    }
}

// 加载监控目录内容
function loadMonitorPane(pane) {
    fetch('/dossier/monitor-pane')
        .then(res => res.text())
        .then(html => {
            pane.innerHTML = html;
            // 直接调用数据加载，不依赖 monitor.html 内的 script
            loadDirectoryList();
        });
}


//文件详情标签页全局函数
window.openFileDetailTab = function(fileId, fileName) {
    const tabName = 'file-' + fileId;
    let existing = document.querySelector(`.tab-item[data-tab="${tabName}"]`);
    if (existing) {
        switchTab(tabName);
        return;
    }
    const tabNav = document.getElementById('tabNav');
    const tab = document.createElement('span');
    tab.className = 'tab-item';
    tab.setAttribute('data-tab', tabName);
    tab.innerHTML = `📄 ${fileName} <span class="tab-close" onclick="closeTab(event, '${tabName}')">&times;</span>`;
    tab.onclick = function() { switchTab(tabName); };
    tabNav.appendChild(tab);

    // 创建内容区
    const tabContent = document.getElementById('tabContent');
    const pane = document.createElement('div');
    pane.className = 'tab-pane';
    pane.id = 'pane-' + tabName;
    pane.innerHTML = `<div style="padding: 20px; color: var(--muted);">加载文件详情...</div>`;
    tabContent.appendChild(pane);

    switchTab(tabName);

    // 加载文件详情
    fetch('/dossier/file/' + fileId + '/detail')
        .then(res => res.text())
        .then(html => {
            pane.innerHTML = html;
            // 手动执行内联脚本
            //不再手动执行脚本，Thymeleaf 已渲染好数据
            /*const scripts = pane.querySelectorAll('script');
            scripts.forEach(s => {
                const newScript = document.createElement('script');
                newScript.textContent = s.textContent;
                document.body.appendChild(newScript);
            });*/
        });
};
/*--index_1>*/


/*<monitor*/
// 加载目录列表
function loadDirectoryList() {
    fetch('/dossier/directory/list')
        .then(res => res.json())
        .then(dirs => {
            const dirList = document.getElementById('dirList');
            if (!dirList) return;
            if (dirs.length === 0) {
                dirList.innerHTML = '<div style="padding: 20px; color: var(--muted); text-align: center;">暂无监控目录</div>';
                return;
            }
            dirList.innerHTML = dirs.map((dir, index) => `
                <div class="dir-item ${index === 0 ? 'active' : ''}" 
                     onclick="selectDir(this, ${dir.id})" 
                     data-dir-id="${dir.id}">
                    <span>${dir.dirName || dir.dirPath}</span>
                    <span class="file-count">加载中...</span>
                </div>
            `).join('');
            if (dirs.length > 0) {
                selectDir(document.querySelector('.dir-item'), dirs[0].id);
            }
        });
}

// 选中目录并加载文件列表
function selectDir(el, dirId) {
    document.querySelectorAll('.dir-item').forEach(d => d.classList.remove('active'));
    el.classList.add('active');
    document.getElementById('filePanelHeader').innerHTML =
        '<i class="bi bi-folder-fill me-2"></i>' + el.querySelector('span').innerText;

    fetch('/dossier/file/list?dirId=' + dirId)
        .then(res => res.json())
        .then(files => {
            el.querySelector('.file-count').textContent = files.length + ' 个文件';
            const fileList = document.getElementById('fileList');
            if (files.length === 0) {
                fileList.innerHTML = '<div style="padding: 40px; color: var(--muted); text-align: center;">此目录下暂无文件</div>';
                return;
            }
            fileList.innerHTML = files.map(f => `
                <div class="file-item">
                    <span class="file-name" onclick="openFileDetailTab(${f.id}, '${f.fileName}')">${f.fileName}</span>
                    <div class="file-actions">
                        <a href="#" onclick="event.stopPropagation();">备注</a>
                        <a href="#" onclick="event.stopPropagation();">修订记录</a>
                        <a href="#" onclick="event.stopPropagation();">删除</a>
                    </div>
                </div>
            `).join('');
        });
}

// 添加目录
function addDirectory() {
    const dirPath = prompt('请输入要监控的目录绝对路径：');
    if (!dirPath) return;
    const dirName = prompt('请输入目录别名（可选）：');

    fetch('/dossier/directory/add', {
        method: 'POST',
        headers: {'Content-Type': 'application/x-www-form-urlencoded'},
        body: 'dirPath=' + encodeURIComponent(dirPath) + '&dirName=' + encodeURIComponent(dirName || '')
    })
        .then(res => {
            if (!res.ok) return res.text().then(msg => { throw new Error(msg); });
            return res.json();
        })
        .then(() => loadDirectoryList())
        .catch(err => alert('添加失败：' + err.message));
}
/*--monitor>*/



/*<file-detail*/
let currentFileId = null;

function addRevisionNote(revId) {
    document.getElementById('noteRevisionSelect').value = revId;
    document.getElementById('newNoteContent').focus();
}

function submitNote() {
    console.log('submitNote 被调用');
    // 从标签页 ID 中提取 fileId（标签页格式为 pane-file-{id}）
    const activePane = document.querySelector('.tab-pane.active');
    const fileId = activePane ? activePane.id.replace('pane-file-', '') : null;

    if (!fileId) {
        alert('无法获取文件ID');
        return;
    }

    const content = document.getElementById('newNoteContent').value.trim();
    console.log('content:', content);
    if (!content) { alert('请输入备注内容'); return; }

    const revisionSelect = document.getElementById('noteRevisionSelect');
    const revisionId = (revisionSelect && revisionSelect.value) ? revisionSelect.value : null;
    console.log('revisionId:', revisionId);

    fetch('/dossier/note/add', {
        method: 'POST',
        headers: {'Content-Type': 'application/x-www-form-urlencoded'},
        body: 'fileId=' + fileId + '&content=' + encodeURIComponent(content) + (revisionId ? '&revisionId=' + revisionId : '')
    })
        .then(res => {
            console.log('响应状态:', res.status);
            if (!res.ok) return res.text().then(msg => { throw new Error(msg); });
            return res.json();
        })
        .then(data => {
            console.log('提交成功:', data);
            document.getElementById('newNoteContent').value = '';
            document.getElementById('noteRevisionSelect').value = '';
            refreshNotes(currentFileId);
        })
        .catch(err => {
            console.error('提交失败:', err);
            alert('添加失败：' + err.message);
        });
}

function refreshNotes() {
    fetch('/dossier/file/' + currentFileId + '/detail-data')
        .then(res => res.json())
        .then(data => {
            // 重建备注列表
            const noteArea = document.getElementById('noteArea');
            if (!noteArea) return;

            const notes = data.notes || data || []; // 兼容 data.notes 或直接返回数组
            if (!Array.isArray(notes) || notes.length === 0) {
                noteArea.innerHTML = '<div style="color: var(--muted); padding: 20px; text-align: center;">暂无备注</div>';
            } else {
                noteArea.innerHTML = notes.map(n => `
                    <div class="note-item">
                        <div>${n.content}</div>
                        <div class="note-meta">${new Date(n.createdAt).toLocaleString()} ${n.revisionId ? '· 关联修订' : '· 文件级备注'}</div>
                    </div>
                `).join('');
            }
        })
        .catch(err => {
            console.error('刷新备注失败:', err);
        });
}
/*--file-detail>*/


/*<calendar*/
function loadDiaryPane(pane) {
    fetch('/diary/calendar/content')
        .then(res => res.text())
        .then(html => {
            pane.innerHTML = html;
            loadData();
        });
}

let currentYear = new Date().getFullYear();
let calendarData = {};

async function loadData() {
    const res = await fetch(`/diary/calendar/year-stats?year=${currentYear}`);
    calendarData = await res.json();
    renderYearGrid();
}

function renderYearGrid() {
    document.getElementById('currentYearLabel').textContent = currentYear;
    const grid = document.getElementById('yearGrid');
    if (!(document.getElementById('currentYearLabel')) || !grid) return;
    grid.innerHTML = '';

    const dayHeaders = ['一', '二', '三', '四', '五', '六', '日'];
    const monthNames = ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月'];

    for (let m = 1; m <= 12; m++) {
        const monthCard = document.createElement('div');
        monthCard.className = 'month-card';

        // 月份标题
        const header = document.createElement('div');
        header.className = 'month-card-header';
        header.textContent = monthNames[m - 1];
        monthCard.appendChild(header);

        // 星期标题
        const miniCal = document.createElement('div');
        miniCal.className = 'mini-calendar';
        dayHeaders.forEach(d => {
            const dh = document.createElement('div');
            dh.className = 'mini-day-header';
            dh.textContent = d;
            miniCal.appendChild(dh);
        });

        const firstDay = new Date(currentYear, m - 1, 1);
        const lastDay = new Date(currentYear, m, 0);
        const totalDays = lastDay.getDate();
        let startDow = firstDay.getDay(); // 0=周日
        startDow = startDow === 0 ? 7 : startDow;

        // 填充空白
        for (let i = 1; i < startDow; i++) {
            const empty = document.createElement('div');
            empty.className = 'mini-day empty';
            miniCal.appendChild(empty);
        }

        // 填充日期
        for (let d = 1; d <= totalDays; d++) {
            const dateStr = `${currentYear}-${String(m).padStart(2, '0')}-${String(d).padStart(2, '0')}`;
            const count = calendarData[dateStr] || 0;
            const dayDiv = document.createElement('div');
            dayDiv.className = 'mini-day' + (count > 0 ? ' has-diary' : '');
            dayDiv.textContent = d;
            if (count > 0) {
                dayDiv.title = `${count}篇日记`;
                // dayDiv.onclick = () => openMonthView(m);
                // 不再设置 dayDiv.onclick，改由 initCalendarEvents 统一绑定
            }
            miniCal.appendChild(dayDiv);
        }

        monthCard.appendChild(miniCal);
        grid.appendChild(monthCard);
        bindMonthCardEvents();
    }
}
function bindMonthCardEvents() {
    document.querySelectorAll('.month-card').forEach(card => {
        card.onclick = function() {
            const monthText = this.querySelector('.month-card-header').textContent;
            openMonthView(parseInt(monthText));
        };
        card.style.cursor = 'pointer';
    });
}

function changeYear(delta) {
    currentYear += delta;
    loadData();
}

function openMonthView(month) {
    // 隐藏年历区域
    document.querySelector('.year-header').style.display = 'none';
    document.getElementById('yearGrid').style.display = 'none';
    // 显示月视图
    const monthView = document.getElementById('monthView');
    monthView.style.display = 'flex';
    document.getElementById('monthTitle').textContent = currentYear + '年' + month + '月';
    // 加载该月数据
    loadMonthData(month);
}

//*<年历变月历新增两个函数*/
async function loadMonthData(month) {
    const res = await fetch(`/diary/calendar/data?year=${currentYear}&month=${month}`);
    const data = await res.json();
    renderMonthGrid(month, data);
}

function renderMonthGrid(month, data) {
    const grid = document.getElementById('monthGrid');
    grid.innerHTML = '';
    const firstDay = new Date(currentYear, month - 1, 1);
    let startDow = firstDay.getDay(); startDow = startDow === 0 ? 7 : startDow;
    const totalDays = new Date(currentYear, month, 0).getDate();

    // 填充空白
    for (let i = 1; i < startDow; i++) {
        const empty = document.createElement('div');
        empty.className = 'month-cell';
        grid.appendChild(empty);
    }
    // 填充日期
    for (let d = 1; d <= totalDays; d++) {
        const ds = `${currentYear}-${String(month).padStart(2, '0')}-${String(d).padStart(2, '0')}`;
        const count = data[ds] || 0;
        const cell = document.createElement('div');
        cell.className = 'month-cell' + (count > 0 ? ' has-diary' : '');
        cell.textContent = d;
        if (count > 0) {
            cell.title = `${count}篇日记`;
            cell.onclick = () => {
                if (window.openDiaryListTab) {
                    window.openDiaryListTab(ds);
                }
            };
        }
        grid.appendChild(cell);
    }
}

function backToYear() {
    document.getElementById('monthView').style.display = 'none';
    document.querySelector('.year-header').style.display = '';
    document.getElementById('yearGrid').style.display = '';
}
//*--年历变月历新增函数>*/

function openWriteDiary() {
    if (window.parent && window.parent.openWriteDiaryTab) {
        window.parent.openWriteDiaryTab();
    }
}

window.openWriteDiaryTab = function() {
    const tabName = 'write-diary';
    let existing = document.querySelector(`.tab-item[data-tab="${tabName}"]`);
    if (existing) {
        switchTab(tabName);
        return;
    }
    // 创建标签页
    const tabNav = document.getElementById('tabNav');
    const tab = document.createElement('span');
    tab.className = 'tab-item';
    tab.setAttribute('data-tab', tabName);
    tab.innerHTML = `✏️ 写日记 <span class="tab-close" onclick="closeTab(event, '${tabName}')">&times;</span>`;
    tab.onclick = function() { switchTab(tabName); };
    tabNav.appendChild(tab);

    const tabContent = document.getElementById('tabContent');
    const pane = document.createElement('div');
    pane.className = 'tab-pane';
    pane.id = 'pane-' + tabName;
    pane.innerHTML = '<div style="padding: 20px; color: var(--muted);">加载写日记页面...</div>';
    tabContent.appendChild(pane);

    switchTab(tabName);

    fetch('/diary/write-page')
        .then(res => res.text())
        .then(html => { pane.innerHTML = html; });
};
/*--calendar>*/

/*<month-calendar*/
//支持年历 → 月历的跳转，需要挂载一个全局函数：
/*window.openMonthCalendar = function(year, month) {
    // 直接打开 /diary/calendar?year=xxxx&month=xx
    const url = '/diary/calendar?year=' + year + '&month=' + month;
    // 在新标签页中打开月历（或者替换当前标签页内容）
    const tabName = 'month-' + year + '-' + month;
    let existing = document.querySelector(`.tab-item[data-tab="${tabName}"]`);
    if (existing) {
        switchTab(tabName);
        return;
    }
    const tabNav = document.getElementById('tabNav');
    const tab = document.createElement('span');
    tab.className = 'tab-item';
    tab.setAttribute('data-tab', tabName);
    tab.innerHTML = `📅 ${year}年${month}月 <span class="tab-close" onclick="closeTab(event, '${tabName}')">&times;</span>`;
    tab.onclick = function() { switchTab(tabName); };
    tabNav.appendChild(tab);

    const tabContent = document.getElementById('tabContent');
    const pane = document.createElement('div');
    pane.className = 'tab-pane';
    pane.id = 'pane-' + tabName;
    pane.innerHTML = `<div style="padding: 20px; color: var(--muted);">加载月历...</div>`;
    tabContent.appendChild(pane);

    switchTab(tabName);

    fetch(url)
        .then(res => res.text())
        .then(html => { pane.innerHTML = html; });
};

var year = /!*[[${year}]]*!/ 2026;
var month = /!*[[${month}]]*!/ 5;
fetch(`/diary/calendar/month?year=${year}&month=${month}`)
    .then(res => res.json())
    .then(data => {
        const grid = document.getElementById('monthGrid');
        // 简化渲染：只显示日期格子
        for (let d = 1; d <= new Date(year, month, 0).getDate(); d++) {
            const ds = `${year}-${String(month).padStart(2,'0')}-${String(d).padStart(2,'0')}`;
            const cnt = data[ds] || 0;
            const cell = document.createElement('div');
            cell.className = 'day-cell' + (cnt > 0 ? ' has-diary' : '');
            cell.innerHTML = `${d}${cnt > 0 ? `<span style="font-size:0.65rem;color:#4a90e2;">·${cnt}</span>` : ''}`;
            if (cnt > 0) cell.onclick = () => alert(`日期:${ds}`);
            grid.appendChild(cell);
        }
    });*/
/*--month-calendar>*/


/*<write-diary*/
function publishDiary() {
    const content = document.getElementById('diaryContent').value.trim();
    if (!content) { alert('请输入日记内容'); return; }

    const isPinned = document.getElementById('pinDiaryCheck').checked;

    // 获取当前天气
    fetch('/diary/calendar/data?year=' + new Date().getFullYear() + '&month=' + (new Date().getMonth() + 1))
        .then(res => res.json())
        .then(() => {
            // 简化处理，显示占位天气
            document.getElementById('currentWeather').textContent = '天气获取中...';
        });

    fetch('/diary/create', {
        method: 'POST',
        headers: {'Content-Type': 'application/x-www-form-urlencoded'},
        body: 'content=' + encodeURIComponent(content) + '&isPinned=' + isPinned
    })
        .then(res => {
            if (!res.ok) return res.text().then(msg => { throw new Error(msg); });
            return res.json();
        })
        .then(() => {
            document.getElementById('diaryContent').value = '';
            document.getElementById('pinDiaryCheck').checked = false;
            const alertBox = document.getElementById('publishAlert');
            alertBox.style.display = 'block';
            setTimeout(() => { alertBox.style.display = 'none'; }, 2000);
        })
        .catch(err => alert('发布失败：' + err.message));
}
/*--write-diary>*/


/*<list*/
window.openDiaryListTab = function(dateStr) {
    const tabName = 'diary-date-' + dateStr;
    let existing = document.querySelector(`.tab-item[data-tab="${tabName}"]`);
    if (existing) {
        switchTab(tabName);
        return;
    }

    const tabNav = document.getElementById('tabNav');
    const tab = document.createElement('span');
    tab.className = 'tab-item';
    tab.setAttribute('data-tab', tabName);
    tab.innerHTML = `📅 ${dateStr} <span class="tab-close" onclick="closeTab(event, '${tabName}')">&times;</span>`;
    tab.onclick = function() { switchTab(tabName); };
    tabNav.appendChild(tab);

    const tabContent = document.getElementById('tabContent');
    const pane = document.createElement('div');
    pane.className = 'tab-pane';
    pane.id = 'pane-' + tabName;
    pane.innerHTML = '<div style="padding: 20px; color: var(--muted);">加载日记列表...</div>';
    tabContent.appendChild(pane);

    switchTab(tabName);

    fetch('/diary/list?date=' + dateStr)
        .then(res => res.text())
        .then(html => { pane.innerHTML = html; });
};

window.openDiaryDetailTab = function(diaryId) {
    const tabName = 'diary-detail-' + diaryId;
    let existing = document.querySelector(`.tab-item[data-tab="${tabName}"]`);
    if (existing) {
        switchTab(tabName);
        return;
    }

    const tabNav = document.getElementById('tabNav');
    const tab = document.createElement('span');
    tab.className = 'tab-item';
    tab.setAttribute('data-tab', tabName);
    tab.innerHTML = `📖 日记详情 <span class="tab-close" onclick="closeTab(event, '${tabName}')">&times;</span>`;
    tab.onclick = function() { switchTab(tabName); };
    tabNav.appendChild(tab);

    const tabContent = document.getElementById('tabContent');
    const pane = document.createElement('div');
    pane.className = 'tab-pane';
    pane.id = 'pane-' + tabName;
    pane.innerHTML = '<div style="padding: 20px; color: var(--muted);">加载日记详情...</div>';
    tabContent.appendChild(pane);

    switchTab(tabName);

    fetch('/diary/' + diaryId)
        .then(res => res.text())
        .then(html => { pane.innerHTML = html; });
};

function openDiaryDetail(diaryId) {
    if (window.parent && window.parent.openDiaryDetailTab) {
        window.parent.openDiaryDetailTab(diaryId);
    }

    // 从当前激活的标签页中获取 pane
    const activePane = document.querySelector('.tab-pane.active');
    if (!activePane) return;

    fetch('/diary/' + diaryId)
        .then(res => res.text())
        .then(html => {
            activePane.innerHTML = html;
            /*const scripts = activePane.querySelectorAll('script');
            scripts.forEach(s => {
                const newScript = document.createElement('script');
                newScript.textContent = s.textContent;
                document.body.appendChild(newScript);
            });*/

            // --- 关键：手动激活这个新页面的段评功能 ---
            activateComments(activePane, diaryId);
        });
}
/*--list>*/


/*<diary/detail.html*/
/*
const diaryId = document.body.getAttribute('data-diary-id');

// 加载已有段评
function loadComments() {
    fetch('/diary/' + diaryId + '/comments')
        .then(res => res.json())
        .then(comments => {
            const list = document.getElementById('commentsList');
            if (comments.length === 0) {
                list.innerHTML = '<p style="color: #94a3b8; font-size: 0.85rem;">暂无段评，划选正文文字添加</p>';
                return;
            }
            list.innerHTML = comments.map(c => `
                    <div class="comment-item">
                        <div class="comment-selected">📌 ${c.selectedText}</div>
                        <div class="comment-text">${c.comment}</div>
                        <div class="comment-meta">v${c.version} · ${new Date(c.createdAt).toLocaleString()}</div>
                    </div>
                `).join('');
        });
}
*/

//负责在新加载的日记详情页上，重新建立划词监听和段评加载逻辑。
/*function activateComments(container, diaryId) {
    const contentEl = container.querySelector('#diaryContent');
    if (!contentEl) return;

    // 1. 先加载已有段评
    fetch('/diary/' + diaryId + '/comments')
        .then(res => res.json())
        .then(comments => {
            const list = container.querySelector('#commentsList');
            if (!list) return;
            if (!comments || comments.length === 0) {
                list.innerHTML = '<p style="color: #94a3b8; font-size: 0.85rem;">暂无段评，划选正文文字添加</p>';
                return;
            }
            list.innerHTML = comments.map(c => `
                <div class="comment-item">
                    <div class="comment-selected">📌 ${c.selectedText}</div>
                    <div class="comment-text">${c.comment}</div>
                    <div class="comment-meta">v${c.version} · ${new Date(c.createdAt).toLocaleString()}</div>
                </div>
            `).join('');
        });

    // 2. 创建划词弹窗
    const popup = document.createElement('div');
    popup.className = 'comment-popup-btn';
    popup.textContent = '写段评';
    document.body.appendChild(popup);

    // 3. 监听划词事件
    document.addEventListener('mouseup', function(e) {
        const selection = window.getSelection();
        const selectedText = selection.toString().trim();

        console.log('选中内容', selectedText);
        const anchorNode = selection.anchorNode;
        console.log('anchorNode', anchorNode);
        console.log('是否包含', contentEl.contains(anchorNode));

        if (selectedText.length > 0 && contentEl.contains(selection.anchorNode)) {
            const range = selection.getRangeAt(0);
            const rect = range.getBoundingClientRect();
            const startOffset = contentEl.textContent.indexOf(selectedText);

            popup.style.display = 'block';
            popup.style.top = (rect.bottom + 6) + 'px';
            popup.style.left = (rect.left) + 'px';

            popup.onclick = function() {
                const content = prompt('请输入你的段评：');
                if (content && content.trim()) {
                    fetch('/diary/' + diaryId + '/comments', {
                        method: 'POST',
                        headers: {'Content-Type': 'application/x-www-form-urlencoded'},
                        body: `selectedText=${encodeURIComponent(selectedText)}&startOffset=${startOffset}&content=${encodeURIComponent(content.trim())}`
                    })
                        .then(res => res.json())
                        .then(() => {
                            selection.removeAllRanges();
                            popup.style.display = 'none';
                            // 添加成功后刷新一下列表
                            activateComments(container, diaryId);
                        });
                }
            };
        } else {
            popup.style.display = 'none';
        }
    });
}*/

let currentHandleMouseUp = null;

function activateComments(container, diaryId) {
    const contentEl = container.querySelector('#diaryContent');
    if (!contentEl) return;

    // 1. 加载已有段评，给正文加高亮
    fetch('/diary/' + diaryId + '/comments')
        .then(res => res.json())
        .then(comments => {
            addHighlightsToContent(contentEl, comments);
        });

    // 2. 创建划词弹窗按钮
    const oldPopup = document.getElementById('commentPopup');
    if (oldPopup) oldPopup.remove();

    const popup = document.createElement('div');
    popup.className = 'comment-popup-btn';
    popup.id = 'commentPopup';
    popup.style.display = 'none';
    popup.textContent = '💬 写段评';
    document.body.appendChild(popup);

    // 3. 监听划词事件
    if (currentHandleMouseUp) {
        document.removeEventListener('mouseup', currentHandleMouseUp);
    }

    currentHandleMouseUp = function(e) {
        // 如果点击的是高亮文字，展示段评悬浮框
        if (e.target.classList.contains('commented-text')) {
            const selectedText = e.target.getAttribute('data-selected-text');
            showCommentFloatBox(e, diaryId, selectedText, container);
            popup.style.display = 'none';
            return;
        }

        const selection = window.getSelection();
        const selectedText = selection.toString().trim();

        console.log('选中内容', selectedText);
        const anchorNode = selection.anchorNode;
        console.log('anchorNode', anchorNode);
        console.log('是否包含', contentEl.contains(anchorNode));

        if (selectedText.length > 0 && contentEl.contains(selection.anchorNode)) {
            const range = selection.getRangeAt(0);
            const rect = range.getBoundingClientRect();
            const startOffset = contentEl.textContent.indexOf(selectedText);

            console.log('准备显示弹窗', popup, rect);
            popup.style.display = 'block';
            popup.style.top = (rect.bottom  + 6) + 'px';
            popup.style.left = (rect.left ) + 'px';

            popup.onclick = function() {
                const content = prompt('请输入你的段评：');
                if (content && content.trim()) {
                    fetch('/diary/' + diaryId + '/comments', {
                        method: 'POST',
                        headers: {'Content-Type': 'application/x-www-form-urlencoded'},
                        body: `selectedText=${encodeURIComponent(selectedText)}&startOffset=${startOffset}&content=${encodeURIComponent(content.trim())}`
                    })
                        .then(res => res.json())
                        .then(() => {
                            selection.removeAllRanges();
                            popup.style.display = 'none';
                            activateComments(container, diaryId);
                        });
                }
            };
        } else {
            popup.style.display = 'none';
        }
    };

    document.addEventListener('mouseup', currentHandleMouseUp);
}

// 给正文中被段评过的文字添加高亮标记
function addHighlightsToContent(contentEl, comments) {
    if (!comments || comments.length === 0) return;

    const fullText = contentEl.textContent;
    const fragments = [];
    let lastEnd = 0;

    // 1. 去重并按偏移量排序
    const unique = [];
    const seen = new Set();
    comments.forEach(c => {
        const key = c.startOffset + '|' + (c.startOffset + c.selectedText.length);
        if (!seen.has(key)) {
            seen.add(key);
            unique.push(c);
        }
    });
    unique.sort((a, b) => a.startOffset - b.startOffset);

    // 2. 逐段切割
    unique.forEach(c => {
        const start = c.startOffset;
        const end = start + c.selectedText.length;

        // 边界保护
        if (start < lastEnd || end > fullText.length) return;
        if (fullText.substring(start, end) !== c.selectedText) return;

        if (lastEnd < start) {
            fragments.push(fullText.substring(lastEnd, start));
        }
        fragments.push(`<span class="commented-text" data-selected-text="${c.selectedText.replace(/"/g, '&quot;')}" data-start="${start}">${fullText.substring(start, end)}</span>`);
        lastEnd = end;
    });

    if (lastEnd < fullText.length) {
        fragments.push(fullText.substring(lastEnd));
    }

    contentEl.innerHTML = fragments.join('');
    /*
    // 按 selected_text 去重
    const uniqueTexts = [...new Set(comments.map(c => c.selectedText))];
    let html = contentEl.textContent;

    uniqueTexts.forEach(text => {
        const escaped = text.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
        html = html.replace(
            new RegExp(escaped, 'g'),
            `<span class="commented-text" data-selected-text="${escaped}">${text}</span>`
        );
    });

    contentEl.innerHTML = html;*/
}

// 弹出可拖拽的段评展示悬浮框
function showCommentFloatBox(event, diaryId, selectedText, container) {
    // 移除旧悬浮框
    const oldBox = document.getElementById('commentFloatBox');
    if (oldBox) oldBox.remove();

    // 获取该段文字的所有段评
    fetch('/diary/' + diaryId + '/comments')
        .then(res => res.json())
        .then(comments => {
            const related = comments.filter(c => c.selectedText === selectedText);
            if (related.length === 0) return;

            // 创建悬浮框
            const box = document.createElement('div');
            box.id = 'commentFloatBox';
            box.style.cssText = `
                position: absolute; top: ${event.pageY}px; left: ${event.pageX}px;
                width: 360px; max-height: 400px; overflow-y: auto;
                background: #fdfdfb; border: 1px solid #b0a99f;
                box-shadow: 0 4px 12px rgba(0,0,0,0.12); z-index: 10000;
                font-family: "Segoe UI","宋体",SimSun,serif;
                display: flex; flex-direction: column;
            `;

            // 标题栏（可拖拽）——固定不动
            const header = document.createElement('div');
            header.style.cssText = `
                display: flex; justify-content: space-between; align-items: center;
                padding: 10px 14px; background: #4a6a8a; color: #fff;
                cursor: move; user-select: none; font-size: 0.9rem; font-weight: 600;
                flex-shrink: 0;
            `;
            header.innerHTML = `<span>📌 段评</span><span style="cursor:pointer; font-size:1.2rem;" onclick="document.getElementById('commentFloatBox').remove()">✕</span>`;
            box.appendChild(header);

            // 段评列表
            const list = document.createElement('div');
            list.style.cssText = 'padding: 10px 14px; flex: 1; overflow-y: auto;';
            related.forEach(c => {
                const item = document.createElement('div');
                item.style.cssText = `
                    padding: 8px 0; border-bottom: 1px solid #e0dbd1; font-size: 0.85rem;
                `;
                item.innerHTML = `
                    <div style="color:#4a6a8a; font-style:italic; margin-bottom:4px;">"${c.selectedText}"</div>
                    <div style="color:#2b2b2b; margin-bottom:2px;">${c.comment}</div>
                    <div style="color:#94a3b8; font-size:0.75rem;">v${c.version} · ${new Date(c.createdAt).toLocaleString()}</div>
                `;
                list.appendChild(item);
            });
            box.appendChild(list);

            // 底部输入区——固定不动
            const footer = document.createElement('div');
            footer.style.cssText = 'padding: 8px 14px; border-top: 1px solid #e0dbd1; display: flex; gap: 8px;';
            const input = document.createElement('input');
            input.placeholder = '追加段评...';
            input.style.cssText = 'flex:1; border:1px solid #b0a99f; padding:6px 10px; font-size:0.85rem; flex-shrink: 0;';
            const submitBtn = document.createElement('button');
            submitBtn.textContent = '提交';
            submitBtn.style.cssText = `
                background: #4a6a8a; color: #fff; border: none; padding: 6px 14px;
                cursor: pointer; font-size: 0.85rem;
            `;
            submitBtn.onclick = function() {
                const content = input.value.trim();
                if (!content) return;
                const startOffset = container.querySelector('#diaryContent').textContent.indexOf(selectedText);
                fetch('/diary/' + diaryId + '/comments', {
                    method: 'POST',
                    headers: {'Content-Type': 'application/x-www-form-urlencoded'},
                    body: `selectedText=${encodeURIComponent(selectedText)}&startOffset=${startOffset}&content=${encodeURIComponent(content)}`
                })
                    .then(res => res.json())
                    .then(() => {
                        box.remove();
                        activateComments(container, diaryId);
                    });
            };
            footer.appendChild(input);
            footer.appendChild(submitBtn);
            box.appendChild(footer);

            // 拖拽功能
            let isDragging = false, offsetX, offsetY;
            header.addEventListener('mousedown', function(e) {
                isDragging = true;
                offsetX = e.clientX - box.offsetLeft;
                offsetY = e.clientY - box.offsetTop;
            });
            document.addEventListener('mousemove', function(e) {
                if (isDragging) {
                    box.style.left = (e.clientX - offsetX) + 'px';
                    box.style.top = (e.clientY - offsetY) + 'px';
                }
            });
            document.addEventListener('mouseup', function() { isDragging = false; });

            container.appendChild(box);
        });
}
/*--diary/detail.html>*/