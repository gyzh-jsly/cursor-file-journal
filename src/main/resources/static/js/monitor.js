////目录列表动态渲染：
// 页面加载时获取真实目录列表
//document.addEventListener('DOMContentLoaded', function() {
    fetch('/dossier/directory/list')
        .then(res => res.json())
        .then(dirs => {
            const dirList = document.getElementById('dirList');
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
            // 默认选中第一个目录
            if (dirs.length > 0) {
                selectDir(document.querySelector('.dir-item'), dirs[0].id);
            }
        });
//});

function selectDir(el, dirId) {
    document.querySelectorAll('.dir-item').forEach(d => d.classList.remove('active'));
    el.classList.add('active');
    document.getElementById('filePanelHeader').innerHTML =
        '<i class="bi bi-folder-fill me-2"></i>' + el.querySelector('span').innerText;

    // 加载该目录下的文件列表
    fetch('/dossier/file/list?dirId=' + dirId)
        .then(res => res.json())
        .then(files => {
            // 更新目录的文件计数
            el.querySelector('.file-count').textContent = files.length + ' 个文件';

            const fileList = document.getElementById('fileList');
            if (files.length === 0) {
                fileList.innerHTML = '<div style="padding: 40px; color: var(--muted); text-align: center;">此目录下暂无文件</div>';
                return;
            }
            fileList.innerHTML = files.map(f => `
                <div class="file-item">
                    <span class="file-name" onclick="openFileTab(${f.id}, '${f.fileName}')">${f.fileName}</span>
                    <div class="file-actions">
                        <a href="#" onclick="event.stopPropagation();">备注</a>
                        <a href="#" onclick="event.stopPropagation();">修订记录</a>
                        <a href="#" onclick="event.stopPropagation();">删除</a>
                    </div>
                </div>
            `).join('');
        });
}

////添加目录功能
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
        .then(newDir => {
            // 刷新目录列表
            location.reload();
        })
        .catch(err => alert('添加失败：' + err.message));
}