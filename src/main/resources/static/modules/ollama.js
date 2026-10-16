function OllamaModule(config, container) {
    const baseUrl = config.baseUrl || 'http://localhost:11434';
    const model = config.model || 'llama3.2';
    const timeout = config.timeout || 30000;
    const systemPrompt = config.systemPrompt || 'You are a helpful assistant.';
    const temperature = config.temperature || 0.7;
    const maxTokens = config.maxTokens || 2000;
    const streamDefault = config.stream === true;

    const styles = `
        .ollama-module { padding: 0.5rem; display: flex; flex-direction: column; height: 100%; min-height: 300px; }
        .ollama-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 0.75rem; padding-bottom: 0.5rem; border-bottom: 1px solid var(--border-color); }
        .ollama-header h4 { margin: 0; font-size: 1rem; color: var(--text-primary); }
        .ollama-model-badge { font-size: 0.7rem; padding: 0.2rem 0.5rem; background: var(--bg-tertiary); border-radius: var(--radius); color: var(--text-secondary); font-family: monospace; }
        .ollama-messages { flex: 1; overflow-y: auto; margin-bottom: 0.75rem; padding: 0.5rem; background: var(--bg-tertiary); border-radius: var(--radius); min-height: 200px; max-height: 400px; }
        .ollama-message { margin-bottom: 0.75rem; padding: 0.75rem; border-radius: var(--radius); max-width: 85%; }
        .ollama-message.user { background: var(--primary-color); color: var(--text-on-primary); margin-left: auto; border-bottom-right-radius: 4px; }
        .ollama-message.assistant { background: var(--bg-hover); color: var(--text-primary); margin-right: auto; border-bottom-left-radius: 4px; }
        .ollama-message.error { background: var(--error-bg); color: var(--danger-color); border: 1px solid var(--danger-color); }
        .ollama-message .meta { font-size: 0.7rem; opacity: 0.7; margin-top: 0.25rem; text-align: right; }
        .ollama-message.user .meta { text-align: right; }
        .ollama-input-area { display: flex; flex-direction: column; gap: 0.5rem; }
        .ollama-input-area textarea { width: 100%; padding: 0.5rem; border: 1px solid var(--input-border); border-radius: var(--radius); font-family: inherit; font-size: 0.875rem; resize: vertical; min-height: 60px; max-height: 150px; background: var(--input-bg); color: var(--text-primary); }
        .ollama-input-area textarea:focus { outline: none; border-color: var(--primary-color); box-shadow: var(--input-focus-shadow); }
        .ollama-controls { display: flex; justify-content: flex-end; gap: 0.5rem; }
        .ollama-loading { display: inline-block; width: 12px; height: 12px; border: 2px solid var(--border-color); border-top-color: var(--primary-color); border-radius: 50%; animation: spin 0.8s linear infinite; margin-right: 0.5rem; vertical-align: middle; }
        @keyframes spin { to { transform: rotate(360deg); } }
        .ollama-empty { text-align: center; color: var(--text-muted); padding: 2rem; font-size: 0.875rem; }
        .ollama-stream-toggle { display: inline-flex; align-items: center; gap: 0.25rem; margin-right: auto; font-size: 0.8125rem; color: var(--text-secondary); cursor: pointer; user-select: none; }
        .ollama-stream-toggle input { cursor: pointer; }
    `;

    container.innerHTML = `
        <div class="ollama-module">
            <style>${styles}</style>
            <div class="ollama-header">
                <h4>Private AI Chat</h4>
                <span class="ollama-model-badge">${model}</span>
            </div>
            <div class="ollama-messages" id="ollama-messages"></div>
            <div class="ollama-input-area">
                <textarea id="ollama-input" placeholder="Ask something..." rows="2"></textarea>
                <div class="ollama-controls">
                    <label class="ollama-stream-toggle" title="Show text as it is generated">
                        <input type="checkbox" id="ollama-stream-toggle"${streamDefault ? ' checked' : ''}>Stream
                    </label>
                    <button id="ollama-send" class="btn btn-primary btn-sm">Send</button>
                    <button id="ollama-clear" class="btn btn-secondary btn-sm">Clear</button>
                </div>
            </div>
        </div>
    `;

    const messagesContainer = container.querySelector('#ollama-messages');
    const input = container.querySelector('#ollama-input');
    const sendBtn = container.querySelector('#ollama-send');
    const clearBtn = container.querySelector('#ollama-clear');

    let isLoading = false;

    function escapeHtml(text) {
        const div = document.createElement('div');
        div.textContent = text;
        return div.innerHTML;
    }

    function formatTime() {
        return new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
    }

    function addMessage(role, content, meta) {
        return addMessageInternal(role, content, meta, true);
    }

    function addMessageRaw(role, content, meta) {
        return addMessageInternal(role, content, meta, false);
    }

    function addMessageInternal(role, content, meta, shouldEscape) {
        const div = document.createElement('div');
        div.className = 'ollama-message ' + role;
        const time = formatTime();
        var metaText = meta ? ' \u00b7 ' + meta : '';
        const displayContent = shouldEscape ? escapeHtml(content) : content;
        div.innerHTML = '<div>' + displayContent + '</div><div class="meta">' + (role === 'user' ? 'You' : 'Assistant') + ' \u00b7 ' + time + metaText + '</div>';
        messagesContainer.appendChild(div);
        messagesContainer.scrollTop = messagesContainer.scrollHeight;
        return div;
    }

    function setLoading(loading) {
        isLoading = loading;
        sendBtn.disabled = loading;
        input.disabled = loading;
        if (loading) {
            sendBtn.innerHTML = '<span class="ollama-loading"></span>Sending...';
        } else {
            sendBtn.textContent = 'Send';
        }
    }

    async function sendMessage() {
        const text = input.value.trim();
        if (!text || isLoading) return;

        addMessage('user', text);
        input.value = '';
        input.style.height = 'auto';

        const streaming = container.querySelector('#ollama-stream-toggle').checked;
        const assistantMsg = addMessageRaw('assistant', '<span class="ollama-loading"></span>Thinking...');
        const contentDiv = assistantMsg.querySelector('div');
        setLoading(true);

        try {
            const response = await fetch(baseUrl + '/api/chat', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    model: model,
                    messages: [
                        { role: 'system', content: systemPrompt },
                        { role: 'user', content: text }
                    ],
                    stream: streaming,
                    options: {
                        temperature: temperature,
                        num_predict: maxTokens
                    }
                }),
                signal: AbortSignal.timeout(timeout)
            });

            if (!response.ok) {
                const error = await response.json().catch(function() { return { error: 'HTTP ' + response.status }; });
                throw new Error(error.error || ('HTTP ' + response.status));
            }

            var meta = '';

            if (streaming) {
                let content = '';
                const reader = response.body.getReader();
                const decoder = new TextDecoder();
                let buffer = '';

                while (true) {
                    const { done, value } = await reader.read();
                    if (done) break;
                    buffer += decoder.decode(value, { stream: true });

                    let nl;
                    while ((nl = buffer.indexOf('\n')) !== -1) {
                        const line = buffer.slice(0, nl).trim();
                        buffer = buffer.slice(nl + 1);
                        if (!line) continue;

                        let chunk;
                        try {
                            chunk = JSON.parse(line);
                        } catch (err) {
                            continue;
                        }

                        if (chunk.message && chunk.message.content) {
                            content += chunk.message.content;
                            contentDiv.innerHTML = escapeHtml(content);
                            messagesContainer.scrollTop = messagesContainer.scrollHeight;
                        }

                        if (chunk.done) {
                            if (chunk.eval_count) meta += chunk.eval_count + ' tokens';
                            if (chunk.eval_duration) meta += (meta ? ' ' : '') + 'in ' + (chunk.eval_duration / 1e9).toFixed(1) + 's';
                        }
                    }
                }

                assistantMsg.innerHTML = '<div>' + escapeHtml(content) + '</div><div class="meta">Assistant \u00b7 ' + formatTime() + (meta ? ' \u00b7 ' + meta : '') + '</div>';
            } else {
                const data = await response.json();
                const content = data.message && data.message.content ? data.message.content : 'No response';

                if (data.eval_count) meta += data.eval_count + ' tokens';
                if (data.eval_duration) meta += ' in ' + (data.eval_duration / 1e9).toFixed(1) + 's';

                assistantMsg.innerHTML = '<div>' + escapeHtml(content) + '</div><div class="meta">Assistant \u00b7 ' + formatTime() + (meta ? ' \u00b7 ' + meta : '') + '</div>';
            }
            messagesContainer.scrollTop = messagesContainer.scrollHeight;

        } catch (e) {
            console.error('Ollama error:', e);
            var errorMsg = e.name === 'AbortError' ? 'Request timed out' : (e.message || 'Failed to get response');
            assistantMsg.className = 'ollama-message error';
            assistantMsg.innerHTML = '<div>' + escapeHtml(errorMsg) + '</div><div class="meta">Error \u00b7 ' + formatTime() + '</div>';
        } finally {
            setLoading(false);
        }
    }

    function clearChat() {
        messagesContainer.innerHTML = '<div class="ollama-empty">Chat cleared. Start a new conversation.</div>';
    }

    sendBtn.addEventListener('click', sendMessage);
    clearBtn.addEventListener('click', clearChat);
    
    input.addEventListener('keydown', function(e) {
        if (e.key === 'Enter' && !e.shiftKey) {
            e.preventDefault();
            sendMessage();
        }
    });

    input.addEventListener('input', function() {
        input.style.height = 'auto';
        input.style.height = Math.min(input.scrollHeight, 150) + 'px';
    });

    if (messagesContainer.children.length === 0) {
        messagesContainer.innerHTML = '<div class="ollama-empty">Welcome! Ask me anything.</div>';
    }
}

if (typeof window !== 'undefined') {
    window.OllamaModule = OllamaModule;
}