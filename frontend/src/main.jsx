import React, { useEffect, useMemo, useState } from "react";
import { createRoot } from "react-dom/client";
import {
  CheckCircle2,
  ClipboardList,
  Loader2,
  LogOut,
  Plus,
  RefreshCw,
  Shield,
  Trash2,
  UserPlus,
  Pencil,
  Search,
  Users,
  Moon,
  Sun
} from "lucide-react";
import { apiRequest, decodeJwt } from "./api";
import "./styles.css";

const emptyTask = {
  id: "",
  title: "",
  description: "",
  status: "PENDING"
};

function getSession() {
  const token = localStorage.getItem("task_token");
  if (!token) {
    return null;
  }

  const payload = decodeJwt(token);
  return {
    token,
    email: payload?.sub || "Signed in",
    role: payload?.role || "USER"
  };
}

function App() {
  const [session, setSession] = useState(getSession);
  const [theme, setTheme] = useState(() => localStorage.getItem("task_theme") || "light");
  const [mode, setMode] = useState("login");
  const [authMessage, setAuthMessage] = useState("");
  const [authError, setAuthError] = useState("");
  const [tasks, setTasks] = useState([]);
  const [filter, setFilter] = useState("ALL");
  const [taskForm, setTaskForm] = useState(emptyTask);
  const [lookupId, setLookupId] = useState("");
  const [lookupTask, setLookupTask] = useState(null);
  const [taskMessage, setTaskMessage] = useState("");
  const [loading, setLoading] = useState(false);
  const [users, setUsers] = useState([]);
  const [userMessage, setUserMessage] = useState("");
  const [loadingUsers, setLoadingUsers] = useState(false);
  const [userSearch, setUserSearch] = useState("");
  const [userRoleFilter, setUserRoleFilter] = useState("ALL");
  const [adminView, setAdminView] = useState("tasks");

  const isAdmin = Boolean(session?.role?.includes("ADMIN"));
  const isDark = theme === "dark";

  const filteredTasks = useMemo(() => {
    if (filter === "ALL") {
      return tasks;
    }
    return tasks.filter((task) => task.status === filter);
  }, [tasks, filter]);

  const counts = useMemo(() => ({
    total: tasks.length,
    pending: tasks.filter((task) => task.status === "PENDING").length,
    progress: tasks.filter((task) => task.status === "IN_PROGRESS").length,
    completed: tasks.filter((task) => task.status === "COMPLETED").length
  }), [tasks]);

  const filteredUsers = useMemo(() => {
    const search = userSearch.trim().toLowerCase();

    return users.filter((user) => {
      const role = normalizeRole(user.role);
      const matchesRole = userRoleFilter === "ALL" || role === userRoleFilter;
      const searchable = [
        user.id,
        user.firstName,
        user.lastName,
        user.email,
        role,
        user.enabled ? "enabled" : "disabled"
      ].join(" ").toLowerCase();

      return matchesRole && (!search || searchable.includes(search));
    });
  }, [users, userSearch, userRoleFilter]);

  const userCounts = useMemo(() => ({
    total: users.length,
    admins: users.filter((user) => normalizeRole(user.role) === "ADMIN").length,
    active: users.filter((user) => user.enabled).length,
    disabled: users.filter((user) => !user.enabled).length
  }), [users]);

  useEffect(() => {
    if (session) {
      loadTasks();
      if (Boolean(session.role?.includes("ADMIN"))) {
        loadUsers();
      }
    }
  }, [session]);

  useEffect(() => {
    document.documentElement.dataset.theme = theme;
    localStorage.setItem("task_theme", theme);
  }, [theme]);

  function toggleTheme() {
    setTheme((currentTheme) => currentTheme === "dark" ? "light" : "dark");
  }

  async function login(event) {
    event.preventDefault();
    setAuthError("");
    setAuthMessage("Signing in...");

    const form = new FormData(event.currentTarget);

    try {
      const data = await apiRequest("/auth/login", {
        method: "POST",
        body: JSON.stringify({
          email: form.get("email").trim(),
          password: form.get("password")
        })
      });

      localStorage.setItem("task_token", data.token);
      setSession(getSession());
      setAuthMessage("");
    } catch (error) {
      setAuthError(error.message);
      setAuthMessage("");
    }
  }

  async function register(event) {
    event.preventDefault();
    setAuthError("");
    setAuthMessage("Creating account...");

    const registerForm = event.currentTarget;
    const form = new FormData(registerForm);

    try {
      await apiRequest("/auth/register", {
        method: "POST",
        body: JSON.stringify({
          firstName: form.get("firstName").trim(),
          lastName: form.get("lastName").trim(),
          email: form.get("email").trim(),
          password: form.get("password")
        })
      });

      registerForm.reset();
      setMode("login");
      setAuthMessage("Account created. Login to continue.");
    } catch (error) {
      setAuthError(error.message);
      setAuthMessage("");
    }
  }

  async function loadTasks() {
    setLoading(true);
    setTaskMessage("");

    try {
      const data = await apiRequest("/tasks", {}, session.token);
      setTasks(data);
    } catch (error) {
      setTaskMessage(error.message);
      if (error.message.toLowerCase().includes("unauthorized")) {
        logout();
      }
    } finally {
      setLoading(false);
    }
  }

  async function saveTask(event) {
    event.preventDefault();
    setTaskMessage(taskForm.id ? "Updating task..." : "Creating task...");

    const payload = {
      title: taskForm.title.trim(),
      description: taskForm.description.trim(),
      status: taskForm.status
    };

    try {
      if (taskForm.id) {
        await apiRequest(`/tasks/${taskForm.id}`, {
          method: "PUT",
          body: JSON.stringify(payload)
        }, session.token);
      } else {
        await apiRequest("/tasks", {
          method: "POST",
          body: JSON.stringify(payload)
        }, session.token);
      }

      setTaskForm(emptyTask);
      await loadTasks();
      setTaskMessage(taskForm.id ? "Task updated." : "Task created.");
    } catch (error) {
      setTaskMessage(error.message);
    }
  }

  async function deleteTask(task) {
    const confirmed = window.confirm(`Delete "${task.title}"?`);
    if (!confirmed) {
      return;
    }

    setTaskMessage("Deleting task...");

    try {
      await apiRequest(`/tasks/${task.id}`, { method: "DELETE" }, session.token);
      await loadTasks();
      setTaskMessage("Task deleted.");
    } catch (error) {
      setTaskMessage(error.message);
    }
  }

  async function findTaskById(event) {
    event.preventDefault();
    setTaskMessage("Finding task...");
    setLookupTask(null);

    try {
      const endpoint = isAdmin ? `/admin/tasks/${lookupId}` : `/tasks/${lookupId}`;
      const data = await apiRequest(endpoint, {}, session.token);
      setLookupTask(data);
      setTaskMessage("Task found.");
    } catch (error) {
      setTaskMessage(error.message);
    }
  }

  async function loadUsers() {
    if (!session?.role?.includes("ADMIN")) {
      return;
    }

    setLoadingUsers(true);
    setUserMessage("");

    try {
      const data = await apiRequest("/admin/users", {}, session.token);
      setUsers(data);
    } catch (error) {
      setUserMessage(error.message);
    } finally {
      setLoadingUsers(false);
    }
  }

  async function updateUser(user, changes) {
    setUserMessage("Updating user...");

    try {
      await apiRequest(`/admin/users/${user.id}`, {
        method: "PUT",
        body: JSON.stringify({
          role: changes.role ?? user.role,
          enabled: changes.enabled ?? user.enabled
        })
      }, session.token);

      await loadUsers();
      setUserMessage("User updated.");
    } catch (error) {
      setUserMessage(error.message);
    }
  }

  async function deleteUser(user) {
    const confirmed = window.confirm(`Delete user "${user.email}" and all assigned tasks?`);
    if (!confirmed) {
      return;
    }

    setUserMessage("Deleting user...");

    try {
      await apiRequest(`/admin/users/${user.id}`, { method: "DELETE" }, session.token);
      await loadUsers();
      await loadTasks();
      setUserMessage("User deleted.");
    } catch (error) {
      setUserMessage(error.message);
    }
  }

  function logout() {
    localStorage.removeItem("task_token");
    setSession(null);
    setTasks([]);
    setUsers([]);
    setTaskForm(emptyTask);
    setAdminView("tasks");
  }

  if (!session) {
    return (
      <main className="auth-page">
        <button className="theme-toggle floating-theme-toggle" type="button" onClick={toggleTheme}>
          {isDark ? <Sun size={18} /> : <Moon size={18} />}
          {isDark ? "Light" : "Dark"}
        </button>
        <section className="hero-panel">
          <div className="hero-content">
            <span className="eyebrow">Task Management System</span>
            <h1>Manage work with secure task access.</h1>
            <p>Sign in to create, update, and track tasks. Admin accounts can review tasks across the system.</p>
          </div>
        </section>

        <section className="auth-card" aria-label="Authentication">
          <div className="segmented-control">
            <button className={mode === "login" ? "active" : ""} type="button" onClick={() => setMode("login")}>
              Login
            </button>
            <button className={mode === "register" ? "active" : ""} type="button" onClick={() => setMode("register")}>
              Register
            </button>
          </div>

          {mode === "login" ? (
            <form className="form-stack" onSubmit={login}>
              <label>Email<input name="email" type="email" required placeholder="example@example.com" /></label>
              <label>Password<input name="password" type="password" required placeholder="Password@123" /></label>
              <button className="primary-button" type="submit">Login</button>
            </form>
          ) : (
            <form className="form-stack" onSubmit={register}>
              <div className="two-column">
                <label>First name<input name="firstName" required placeholder="FirstName" /></label>
                <label>Last name<input name="lastName" required placeholder="LastName" /></label>
              </div>
              <label>Email<input name="email" type="email" required placeholder="example@example.com" /></label>
              <label>Password<input name="password" type="password" required placeholder="Password@123" /></label>
              <button className="primary-button" type="submit"><UserPlus size={18} /> Create account</button>
            </form>
          )}

          {authMessage && <p className="message success">{authMessage}</p>}
          {authError && <p className="message error">{authError}</p>}
        </section>
      </main>
    );
  }

  return (
    <main className="dashboard-page">
      <header className="topbar">
        <div>
          <span className="eyebrow">Workspace</span>
          <h1>{isAdmin && adminView === "users" ? "User details" : isAdmin ? "All tasks" : "My tasks"}</h1>
        </div>
        <div className="account-strip">
          <span>{session.email}</span>
          <span className="role-badge"><Shield size={14} /> {isAdmin ? "ADMIN" : "USER"}</span>
          <button className="ghost-button" type="button" onClick={toggleTheme}>
            {isDark ? <Sun size={17} /> : <Moon size={17} />} {isDark ? "Light" : "Dark"}
          </button>
          <button className="ghost-button" type="button" onClick={logout}><LogOut size={17} /> Logout</button>
        </div>
      </header>

      {isAdmin && (
        <nav className="admin-nav" aria-label="Admin sections">
          <button className={adminView === "tasks" ? "active" : ""} type="button" onClick={() => setAdminView("tasks")}>
            <ClipboardList size={17} /> Tasks
          </button>
          <button className={adminView === "users" ? "active" : ""} type="button" onClick={() => setAdminView("users")}>
            <Users size={17} /> Users
          </button>
        </nav>
      )}

      {(!isAdmin || adminView === "tasks") && (
        <>
          <section className="metrics-grid">
            <Metric label="Total" value={counts.total} icon={<ClipboardList />} />
            <Metric label="Pending" value={counts.pending} />
            <Metric label="In progress" value={counts.progress} />
            <Metric label="Completed" value={counts.completed} icon={<CheckCircle2 />} />
          </section>

          <section className="workspace-grid">
            <div className="side-stack">
              <form className="task-form" onSubmit={saveTask}>
                <div className="section-heading">
                  <h2>{taskForm.id ? "Edit task" : "Create task"}</h2>
                  {taskForm.id && (
                    <button className="icon-button" type="button" onClick={() => setTaskForm(emptyTask)}>x</button>
                  )}
                </div>

                <label>Title
                  <input
                    value={taskForm.title}
                    onChange={(event) => setTaskForm({ ...taskForm, title: event.target.value })}
                    minLength={3}
                    maxLength={100}
                    required
                    placeholder="Learn Spring Boot"
                  />
                </label>

                <label>Description
                  <textarea
                    value={taskForm.description}
                    onChange={(event) => setTaskForm({ ...taskForm, description: event.target.value })}
                    minLength={10}
                    maxLength={500}
                    required
                    placeholder="Practice building secure REST APIs with Spring Boot"
                  />
                </label>

                <label>Status
                  <select value={taskForm.status} onChange={(event) => setTaskForm({ ...taskForm, status: event.target.value })}>
                    <option value="PENDING">Pending</option>
                    <option value="IN_PROGRESS">In progress</option>
                    <option value="COMPLETED">Completed</option>
                  </select>
                </label>

                <button className="primary-button" type="submit"><Plus size={18} /> {taskForm.id ? "Update task" : "Create task"}</button>
              </form>

              <section className="lookup-panel">
                <div className="section-heading">
                  <h2>Find task</h2>
                </div>
                <form className="lookup-form" onSubmit={findTaskById}>
                  <label>Task ID
                    <input
                      value={lookupId}
                      onChange={(event) => setLookupId(event.target.value)}
                      type="number"
                      min="1"
                      required
                      placeholder="1"
                    />
                  </label>
                  <button className="primary-button" type="submit">Get task</button>
                </form>

                {lookupTask && (
                  <article className="lookup-card">
                    <div className="task-card-header">
                      <h3>{lookupTask.title}</h3>
                      <span className={`status-badge status-${lookupTask.status}`}>{formatStatus(lookupTask.status)}</span>
                    </div>
                    {isAdmin && <OwnerLabel task={lookupTask} />}
                    <p>{lookupTask.description}</p>
                    <span className="task-meta">ID {lookupTask.id} · Updated {formatDate(lookupTask.updatedAt)}</span>
                    <div className="task-actions">
                      <button className="text-button" type="button" onClick={() => setTaskForm(lookupTask)}><Pencil size={16} /> Edit</button>
                      <button className="text-button danger" type="button" onClick={() => deleteTask(lookupTask)}><Trash2 size={16} /> Delete</button>
                    </div>
                  </article>
                )}
              </section>
            </div>

            <section className="task-panel">
              <div className="list-toolbar">
                <div className="section-heading">
                  <h2>Tasks</h2>
                  <button className="icon-button" type="button" onClick={loadTasks} title="Refresh">
                    {loading ? <Loader2 className="spin" size={18} /> : <RefreshCw size={18} />}
                  </button>
                </div>
                <div className="filters">
                  {["ALL", "PENDING", "IN_PROGRESS", "COMPLETED"].map((status) => (
                    <button
                      key={status}
                      className={filter === status ? "active" : ""}
                      type="button"
                      onClick={() => setFilter(status)}
                    >
                      {formatStatus(status)}
                    </button>
                  ))}
                </div>
              </div>

              <div className="task-list">
                {filteredTasks.length ? filteredTasks.map((task) => (
                  <article className="task-card" key={task.id}>
                    <div className="task-card-header">
                      <h3>{task.title}</h3>
                      <span className={`status-badge status-${task.status}`}>{formatStatus(task.status)}</span>
                    </div>
                    {isAdmin && <OwnerLabel task={task} />}
                    <p>{task.description}</p>
                    <span className="task-meta">Updated {formatDate(task.updatedAt)}</span>
                    <div className="task-actions">
                      <button className="text-button" type="button" onClick={() => setTaskForm(task)}><Pencil size={16} /> Edit</button>
                      <button className="text-button danger" type="button" onClick={() => deleteTask(task)}><Trash2 size={16} /> Delete</button>
                    </div>
                  </article>
                )) : (
                  <div className="empty-state">No tasks found.</div>
                )}
              </div>

              {taskMessage && <p className={taskMessage.includes("Backend") || taskMessage.includes("failed") ? "message error" : "message success"}>{taskMessage}</p>}
            </section>
          </section>
        </>
      )}

      {isAdmin && adminView === "users" && (
        <section className="admin-panel admin-users-page" aria-label="Admin user details">
          <div className="list-toolbar">
            <div className="section-heading">
              <h2><Users size={20} /> User details</h2>
              <button className="icon-button" type="button" onClick={loadUsers} title="Refresh users">
                {loadingUsers ? <Loader2 className="spin" size={18} /> : <RefreshCw size={18} />}
              </button>
            </div>
            <div className="user-tools">
              <label className="search-field">
                <Search size={16} />
                <input
                  value={userSearch}
                  onChange={(event) => setUserSearch(event.target.value)}
                  placeholder="Search users"
                />
              </label>
              <select value={userRoleFilter} onChange={(event) => setUserRoleFilter(event.target.value)}>
                <option value="ALL">All roles</option>
                <option value="ADMIN">Admins</option>
                <option value="USER">Users</option>
              </select>
            </div>
          </div>

          <div className="admin-summary">
            <div><span>Total users</span><strong>{userCounts.total}</strong></div>
            <div><span>Admins</span><strong>{userCounts.admins}</strong></div>
            <div><span>Enabled</span><strong>{userCounts.active}</strong></div>
            <div><span>Disabled</span><strong>{userCounts.disabled}</strong></div>
          </div>

          <div className="user-table">
            <div className="user-row user-row-head">
              <span>ID</span>
              <span>User</span>
              <span>Role</span>
              <span>Status</span>
              <span>Created</span>
              <span>Actions</span>
            </div>
            {filteredUsers.map((user) => (
              <div className="user-row" key={user.id}>
                <span className="user-id">#{user.id}</span>
                <div>
                  <strong>{user.firstName} {user.lastName}</strong>
                  <small>{user.email}</small>
                </div>
                <select value={normalizeRole(user.role)} onChange={(event) => updateUser(user, { role: event.target.value })}>
                  <option value="USER">USER</option>
                  <option value="ADMIN">ADMIN</option>
                </select>
                <label className="switch-label">
                  <input
                    type="checkbox"
                    checked={user.enabled}
                    onChange={(event) => updateUser(user, { enabled: event.target.checked })}
                  />
                  {user.enabled ? "Enabled" : "Disabled"}
                </label>
                <span className="task-meta">{formatDate(user.createdAt)}</span>
                <button className="text-button danger" type="button" onClick={() => deleteUser(user)}>
                  <Trash2 size={16} /> Delete
                </button>
              </div>
            ))}
          </div>

          {!filteredUsers.length && <div className="empty-state">No users found.</div>}
          {userMessage && <p className={userMessage.includes("updated") || userMessage.includes("deleted") ? "message success" : "message error"}>{userMessage}</p>}
        </section>
      )}
    </main>
  );
}

function Metric({ label, value, icon }) {
  return (
    <article className="metric-card">
      <div>
        <span>{label}</span>
        <strong>{value}</strong>
      </div>
      {icon && <div className="metric-icon">{icon}</div>}
    </article>
  );
}

function OwnerLabel({ task }) {
  const ownerName = task.userName || "Unknown user";
  const ownerEmail = task.userEmail || "No email";

  return (
    <div className="owner-label">
      <span>Assigned to</span>
      <strong>{ownerName}</strong>
      <small>{ownerEmail}</small>
    </div>
  );
}

function formatStatus(status) {
  if (status === "ALL") {
    return "All";
  }
  return status.replace("_", " ").toLowerCase().replace(/\b\w/g, (char) => char.toUpperCase());
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

function normalizeRole(role) {
  return role?.replace("ROLE_", "") || "USER";
}

createRoot(document.getElementById("root")).render(<App />);
