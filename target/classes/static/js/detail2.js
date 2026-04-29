
    let currentFileId = null;

    function openFileDetail(element) {
        const fileId = element.getAttribute('data-file-id');
        currentFileId = fileId;
        
        const fileName = element.querySelector('strong').innerText;
        const commitCount = element.querySelector('.badge')?.innerText.replace('v', '') || '0';
        
        document.getElementById('detailFileName').innerText = fileName;
        document.getElementById('detailCommitCount').innerText = commitCount;
        document.getElementById('detailEmptyState').style.display = 'none';
        document.getElementById('detailContent').style.display = 'block';
        
        loadCommitHistory(fileId);
    }

    // function loadCommitHistory(fileId) {
    //     fetch('/commit/history/' + fileId)
    //         .then(response => response.json())
    //         .then(data => {
    //             const file = data.file;
    //             if (file.firstSeenTime) {
    //                 document.getElementById('detailFirstSeen').innerText = 
    //                     new Date(file.firstSeenTime).toLocaleString('zh-CN');
    //             }
    //             document.getElementById('detailFirstWeather').innerText = file.firstWeather || '未记录';
    //             document.getElementById('detailLastActive').innerText = 
    //                 file.lastCommitTime ? new Date(file.lastCommitTime).toLocaleString('zh-CN') : '暂无';
    //             renderTimeline(data.commits);
    //         });
    // }

    function loadCommitHistory(fileId) {
        fetch('/commit/history/' + fileId)
            .then(response => response.json())
            .then(data => {
                const file = data.file;
                
                // 更新修改次数
                document.getElementById('detailCommitCount').innerText = file.commitCount || 0;
                
                // 更新最后活跃时间
                if (file.lastCommitTime) {
                    document.getElementById('detailLastActive').innerText = 
                        new Date(file.lastCommitTime).toLocaleString('zh-CN');
                }
                
                // 更新初见信息（如果还没设置）
                if (file.firstSeenTime) {
                    document.getElementById('detailFirstSeen').innerText = 
                        new Date(file.firstSeenTime).toLocaleString('zh-CN');
                }
                document.getElementById('detailFirstWeather').innerText = file.firstWeather || '未记录';
                
                // 渲染时间轴
                renderTimeline(data.commits);
            });
    }

    function renderTimeline(commits) {
        const timeline = document.getElementById('commitTimeline');
        if (commits.length === 0) {
            timeline.innerHTML = '<p class="text-muted text-center py-4">暂无修改记录</p>';
            return;
        }
        
        let html = '';
        commits.forEach((commit, index) => {
            const time = new Date(commit.commitTime).toLocaleString('zh-CN', {
                month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit'
            });
            const isLatest = index === 0;
            
            html += `
                <div class="timeline-item d-flex mb-3 ${isLatest ? 'border-start border-primary border-3 ps-3' : 'ps-3'}">
                    <div class="flex-shrink-0 text-muted me-3" style="width: 100px;">
                        <small>${time}</small>
                    </div>
                    <div class="flex-grow-1">
                        <div class="d-flex align-items-center">
                            ${isLatest ? '<span class="badge bg-primary me-2">最新</span>' : ''}
                            <span class="text-secondary">v${commits.length - index}</span>
                        </div>
                        <div class="mt-1" style="word-break: break-word; max-width: 100%;">
                            ${commit.message || '<span class="text-muted fst-italic">无备注</span>'}
                        </div>
                    </div>
                </div>
            `;
        });
        timeline.innerHTML = html;
    }

    function openCommitModal(btn) {
        const fileId = btn.getAttribute('data-file-id');
        const filePath = btn.getAttribute('data-file-path');
        const fileName = filePath.split('\\').pop().split('/').pop();
        
        document.getElementById('commitFileId').value = fileId;
        document.getElementById('commitFileName').innerText = fileName;
        document.getElementById('commitMessage').value = '';
        
        new bootstrap.Modal(document.getElementById('commitModal')).show();
    }

    // function submitCommit() {
    //     const fileId = document.getElementById('commitFileId').value;
    //     const message = document.getElementById('commitMessage').value;
        
    //     fetch('/commit/add', {
    //         method: 'POST',
    //         headers: {'Content-Type': 'application/x-www-form-urlencoded'},
    //         body: `fileId=${fileId}&message=${encodeURIComponent(message)}`
    //     }).then(response => {
    //         if (response.ok) {
    //             location.reload();
    //         } else {
    //             alert('记录失败，请重试');
    //         }
    //     });
    // }

    function submitCommit() {
        const fileId = document.getElementById('commitFileId').value;
        const message = document.getElementById('commitMessage').value;
        
        fetch('/commit/add', {
            method: 'POST',
            headers: {'Content-Type': 'application/x-www-form-urlencoded'},
            body: `fileId=${fileId}&message=${encodeURIComponent(message)}`
        }).then(response => {
            if (response.ok) {
                // 1. 关闭模态框
                const modal = bootstrap.Modal.getInstance(document.getElementById('commitModal'));
                modal.hide();
                
                // 2. 刷新右侧档案详情（如果当前选中的文件就是刚提交的这个）
                if (currentFileId == fileId) {
                    loadCommitHistory(fileId);

                    // 关键：同时更新修改次数
                    // updateDetailCommitCount(fileId);
                }
                
                // 3. 刷新中间聚合流列表（更新修改次数和最后修改时间）
                refreshAggregateStream();
            } else {
                alert('记录失败，请重试');
            }
        });
    }

    // function updateDetailCommitCount(fileId) {
    //     // 方法一：从聚合流中对应卡片获取最新的次数
    //     const fileItem = document.querySelector(`.file-item[data-file-id="${fileId}"]`);
    //     if (fileItem) {
    //         const badge = fileItem.querySelector('.badge');
    //         if (badge) {
    //             const newCount = badge.innerText.replace('v', '');
    //             document.getElementById('detailCommitCount').innerText = newCount;
    //         }
    //     }
        
    //     // 方法二（备选）：如果聚合流还没刷新，从后端重新获取
    //     // fetch('/file/' + fileId).then(...)
    // }

    // function refreshAggregateStream() {
    //     const folderId = document.querySelector('.folder-list-item.active')?.getAttribute('data-folder-id');
    //     if (!folderId) {
    //         // 如果无法获取当前文件夹ID，从 URL 中提取
    //         const path = window.location.pathname;
    //         const match = path.match(/\/folder\/view\/(\d+)/);
    //         if (match) folderId = match[1];
    //     }
        
    //     if (folderId) {
    //         fetch(`/folder/aggregate/${folderId}`)
    //             .then(response => response.text())
    //             .then(html => {
    //                 document.querySelector('.stream-list').innerHTML = html;
    //             });
    //     }
    // }
    function refreshAggregateStream() {
        const activeFolder = document.querySelector('.folder-list-item.active');
        if (!activeFolder) return;
        
        const folderId = activeFolder.getAttribute('data-folder-id');
        if (!folderId) return;
        
        fetch('/folder/aggregate/' + folderId)
            .then(response => response.text())
            .then(html => {
                const streamList = document.querySelector('.aggregate-stream .stream-list');
                console.log('容器元素:', streamList);
                if (streamList) {
                    streamList.innerHTML = html;
                    // 重新绑定事件（如果新生成的按钮需要）

                    console.log('替换完成，当前子元素数量:', streamList.children.length);

                    streamList.style.display = 'none';
                    streamList.offsetHeight; // 强制重绘
                    streamList.style.display = '';
                }
            });

    }

    function switchFolder(element, event) {
        event.preventDefault();
        
        const folderId = element.getAttribute('data-folder-id');
        if (!folderId) return;
        
        // 更新高亮
        document.querySelectorAll('.folder-list-item').forEach(item => item.classList.remove('active'));
        element.classList.add('active');
        
        // 刷新聚合流
        fetch(`/folder/aggregate/${folderId}`)
            .then(response => response.text())
            .then(html => {
                const streamList = document.querySelector('.aggregate-stream .stream-list');
                if (streamList) {
                    streamList.innerHTML = html;
                }
            });
        
        // 清空右侧详情区
        const detailContent = document.getElementById('detailContent');
        const detailEmptyState = document.getElementById('detailEmptyState');
        if (detailContent && detailEmptyState) {
            detailContent.style.display = 'none';
            detailEmptyState.style.display = 'flex';
        }
    }

    