const isFilePreview = window.location.protocol === "file:";
const apiBase = "/api/v1";
const state = {
    token: localStorage.getItem("task_token"),
    user: null,
    tasks: [],
    filter: "ALL"
};

const els = {
    authView: document.querySelector("#authView"),
    dashboardView: document.querySelector("#dashboardView"),
    loginTab: document.querySelector("#loginTab"),
    registerTab: document.querySelector("#registerTab"),
    loginForm: document.querySelector("#loginForm"),
    registerForm: document.querySelector("#registerForm"),
    authMessage: document.querySelector("#authMessage"),
    userEmail: document.querySelector("#userEmail"),
    userRole: document.querySelector("#userRole"),
    logoutButton: document.querySelector("#logoutButton"),
    taskForm: document.querySelector("#taskForm"),
    taskId: document.querySelector("#taskId"),
    taskTitle: document.querySelector("#taskTitle"),
    taskDescription: document.querySelector("#taskDescription"),
    taskStatus: document.querySelector("#taskStatus"),
    formTitle: document.querySelector("#formTitle"),
    saveTaskButton: document.querySelector("#saveTaskButton"),
    cancelEditButton: document.querySelector("#cancelEditButton"),
    taskList: document.querySelector("#taskList"),
    taskMessage: document.querySelector("#taskMessage"),
    refreshButton: document.querySelector("#refreshButton"),
    listTitle: document.querySelector("#listTitle"),
    totalCount: document.querySelector("#totalCount"),
    pendingCount: document.querySelector("#pendingCount"),
    progressCount: document.querySelector("#progressCount"),
    completedCount: document.querySelector("#completedCount")
};

function decodeJwt(token) {
    try {
        const payload = token.split(".")[1];
        const json = atob(payload.replace(/-/g, "+").replace(/_/g, "/"));
        return JSON.parse(decodeURIComponent(Array.from(json).map(char => {
            return "%" + char.charCodeAt(0).toString(16).padStart(2, "0");
        }).join("")));
    } catch (error) {
        return null;
    }
}

function isAdmin() {
    return state.user?.role?.includes("ADMIN");
}

function setMessage(element, message, type = "") {
    element.textContent = message || "";
    element.className = `message ${type}`.trim();
}

async function api(path, options = {}) {
    if (isFilePreview) {
        throw new Error("Open this page from Spring Boot: http://localhost:8080/ or http://localhost:8090/");
    }

    const headers = {
        "Content-Type": "application/json",
        ...options.headers
    };

    if (state.token) {
        headers.Authorization = `Bearer ${state.token}`;
    }

    let response;

    try {
        response = await fetch(`${apiBase}${path}`, {
            ...options,
            headers
        });
    } catch (error) {
        throw new Error("Backend is not reachable. Start Spring Boot and open the app from http://localhost:8080/.");
    }

    const contentType = response.headers.get("content-type") || "";
    const body = contentType.includes("application/json")
        ? await response.json()
        : await response.text();

    if (!response.ok) {
        const message = body?.message || body || `Request failed with status ${response.status}`;
        throw new Error(message);
    }

    return body;
}

function setAuthMode(mode) {
    const loginMode = mode === "login";
    els.loginTab.classList.toggle("active", loginMode);
    els.registerTab.classList.toggle("active", !loginMode);
    els.loginForm.classList.toggle("hidden", !loginMode);
    els.registerForm.classList.toggle("hidden", loginMode);
    setMessage(els.authMessage, "");
}

function applySession(token) {
    state.token = token;
    localStorage.setItem("task_token", token);
    const payload = decodeJwt(token);
    state.user = {
        email: payload?.sub || "Signed in",
        role: payload?.role || "USER"
    };
}

function showDashboard() {
    els.authView.classList.add("hidden");
    els.dashboardView.classList.remove("hidden");
    els.userEmail.textContent = state.user.email;
    els.userRole.textContent = isAdmin() ? "ADMIN" : "USER";
    els.listTitle.textContent = isAdmin() ? "All tasks" : "My tasks";
    loadTasks();
}

function showAuth() {
    els.dashboardView.classList.add("hidden");
    els.authView.classList.remove("hidden");
}

async function handleLogin(event) {
    event.preventDefault();
    setMessage(els.authMessage, "Signing in...");

    try {
        const data = await api("/auth/login", {
            method: "POST",
            body: JSON.stringify({
                email: document.querySelector("#loginEmail").value.trim(),
                password: document.querySelector("#loginPassword").value
            })
        });

        applySession(data.token);
        setMessage(els.authMessage, "");
        showDashboard();
    } catch (error) {
        setMessage(els.authMessage, error.message, "error");
    }
}

async function handleRegister(event) {
    event.preventDefault();
    setMessage(els.authMessage, "Creating account...");

    try {
        await api("/auth/register", {
            method: "POST",
            body: JSON.stringify({
                firstName: document.querySelector("#firstName").value.trim(),
                lastName: document.querySelector("#lastName").value.trim(),
                email: document.querySelector("#registerEmail").value.trim(),
                password: document.querySelector("#registerPassword").value
            })
        });

        els.registerForm.reset();
        setAuthMode("login");
        setMessage(els.authMessage, "Account created. Login to continue.", "success");
    } catch (error) {
        setMessage(els.authMessage, error.message, "error");
    }
}

async function loadTasks() {
    setMessage(els.taskMessage, "Loading tasks...");

    try {
        state.tasks = await api("/tasks");
        renderTasks();
        setMessage(els.taskMessage, "");
    } catch (error) {
        setMessage(els.taskMessage, error.message, "error");
        if (error.message.toLowerCase().includes("unauthorized")) {
            logout();
        }
    }
}

function formatStatus(status) {
    return status.replace("_", " ").toLowerCase().replace(/\b\w/g, char => char.toUpperCase());
}

function formatDate(value) {
    if (!value) {
        return "";
    }
    return new Intl.DateTimeFormat(undefined, {
        dateStyle: "medium",
        timeStyle: "short"
    }).format(new Date(value));
}

function updateMetrics() {
    els.totalCount.textContent = state.tasks.length;
    els.pendingCount.textContent = state.tasks.filter(task => task.status === "PENDING").length;
    els.progressCount.textContent = state.tasks.filter(task => task.status === "IN_PROGRESS").length;
    els.completedCount.textContent = state.tasks.filter(task => task.status === "COMPLETED").length;
}

function renderTasks() {
    updateMetrics();

    const tasks = state.filter === "ALL"
        ? state.tasks
        : state.tasks.filter(task => task.status === state.filter);

    if (!tasks.length) {
        els.taskList.innerHTML = `<div class="empty-state">No tasks found.</div>`;
        return;
    }

    els.taskList.innerHTML = tasks.map(task => `
        <article class="task-card">
            <div class="task-card-header">
                <h3>${escapeHtml(task.title)}</h3>
                <span class="status-badge status-${task.status}">${formatStatus(task.status)}</span>
            </div>
            <p>${escapeHtml(task.description)}</p>
            <div class="task-meta">Updated ${formatDate(task.updatedAt)}</div>
            <div class="task-actions">
                <button class="text-button" type="button" data-action="edit" data-id="${task.id}">Edit</button>
                <button class="text-button danger" type="button" data-action="delete" data-id="${task.id}">Delete</button>
            </div>
        </article>
    `).join("");
}

function escapeHtml(value) {
    return String(value ?? "")
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#039;");
}

async function handleTaskSave(event) {
    event.preventDefault();
    const id = els.taskId.value;
    const payload = {
        title: els.taskTitle.value.trim(),
        description: els.taskDescription.value.trim(),
        status: els.taskStatus.value
    };

    setMessage(els.taskMessage, id ? "Updating task..." : "Creating task...");

    try {
        if (id) {
            await api(`/tasks/${id}`, {
                method: "PUT",
                body: JSON.stringify(payload)
            });
        } else {
            await api("/tasks", {
                method: "POST",
                body: JSON.stringify(payload)
            });
        }

        resetTaskForm();
        await loadTasks();
        setMessage(els.taskMessage, id ? "Task updated." : "Task created.", "success");
    } catch (error) {
        setMessage(els.taskMessage, error.message, "error");
    }
}

function editTask(id) {
    const task = state.tasks.find(item => String(item.id) === String(id));
    if (!task) {
        return;
    }

    els.taskId.value = task.id;
    els.taskTitle.value = task.title;
    els.taskDescription.value = task.description;
    els.taskStatus.value = task.status;
    els.formTitle.textContent = "Edit task";
    els.saveTaskButton.textContent = "Update task";
    els.cancelEditButton.classList.remove("hidden");
    els.taskTitle.focus();
}

async function deleteTask(id) {
    const task = state.tasks.find(item => String(item.id) === String(id));
    if (!task) {
        return;
    }

    const confirmed = window.confirm(`Delete "${task.title}"?`);
    if (!confirmed) {
        return;
    }

    setMessage(els.taskMessage, "Deleting task...");

    try {
        await api(`/tasks/${id}`, { method: "DELETE" });
        await loadTasks();
        setMessage(els.taskMessage, "Task deleted.", "success");
    } catch (error) {
        setMessage(els.taskMessage, error.message, "error");
    }
}

function resetTaskForm() {
    els.taskForm.reset();
    els.taskId.value = "";
    els.formTitle.textContent = "Create task";
    els.saveTaskButton.textContent = "Create task";
    els.cancelEditButton.classList.add("hidden");
}

function logout() {
    localStorage.removeItem("task_token");
    state.token = null;
    state.user = null;
    state.tasks = [];
    resetTaskForm();
    showAuth();
}

els.loginTab.addEventListener("click", () => setAuthMode("login"));
els.registerTab.addEventListener("click", () => setAuthMode("register"));
els.loginForm.addEventListener("submit", handleLogin);
els.registerForm.addEventListener("submit", handleRegister);
els.logoutButton.addEventListener("click", logout);
els.refreshButton.addEventListener("click", loadTasks);
els.taskForm.addEventListener("submit", handleTaskSave);
els.cancelEditButton.addEventListener("click", resetTaskForm);

document.querySelectorAll(".filter-button").forEach(button => {
    button.addEventListener("click", () => {
        document.querySelectorAll(".filter-button").forEach(item => item.classList.remove("active"));
        button.classList.add("active");
        state.filter = button.dataset.filter;
        renderTasks();
    });
});

els.taskList.addEventListener("click", event => {
    const button = event.target.closest("button[data-action]");
    if (!button) {
        return;
    }

    if (button.dataset.action === "edit") {
        editTask(button.dataset.id);
    }

    if (button.dataset.action === "delete") {
        deleteTask(button.dataset.id);
    }
});

if (state.token) {
    applySession(state.token);
    showDashboard();
} else {
    showAuth();
}

if (isFilePreview) {
    setMessage(
        els.authMessage,
        "Open this page from Spring Boot, for example http://localhost:8080/, so API calls can work.",
        "error"
    );
}
