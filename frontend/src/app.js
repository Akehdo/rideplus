const STORAGE_KEYS = {
    accessToken: "rideplus.accessToken",
    refreshToken: "rideplus.refreshToken",
    passenger: "rideplus.passenger"
};

const state = {
    mode: "login",
    accessToken: localStorage.getItem(STORAGE_KEYS.accessToken) || "",
    refreshToken: localStorage.getItem(STORAGE_KEYS.refreshToken) || "",
    passenger: readStoredJson(STORAGE_KEYS.passenger)
};

const ui = {
    gatewayUrl: document.getElementById("gateway-url"),
    authForm: document.getElementById("auth-form"),
    passengerForm: document.getElementById("passenger-form"),
    authSubmit: document.getElementById("auth-submit"),
    messageBox: document.getElementById("message-box"),
    sessionTitle: document.getElementById("session-title"),
    sessionBadge: document.getElementById("session-badge"),
    userId: document.getElementById("user-id"),
    accessTokenPreview: document.getElementById("access-token-preview"),
    tokenExpiry: document.getElementById("token-expiry"),
    passengerPreview: document.getElementById("passenger-preview"),
    authHint: document.getElementById("auth-hint"),
    passengerLock: document.getElementById("passenger-lock"),
    passengerSubmit: document.getElementById("passenger-submit"),
    refreshButton: document.getElementById("refresh-button"),
    logoutButton: document.getElementById("logout-button"),
    eventFeed: document.getElementById("event-feed"),
    email: document.getElementById("email"),
    password: document.getElementById("password"),
    firstName: document.getElementById("first-name"),
    lastName: document.getElementById("last-name"),
    authTabs: Array.from(document.querySelectorAll("[data-auth-mode]"))
};

ui.gatewayUrl.textContent = window.location.origin || "http://localhost:8088";

ui.authTabs.forEach((button) => {
    button.addEventListener("click", () => setAuthMode(button.dataset.authMode));
});

ui.authForm.addEventListener("submit", onAuthSubmit);
ui.passengerForm.addEventListener("submit", onPassengerSubmit);
ui.refreshButton.addEventListener("click", onRefreshClick);
ui.logoutButton.addEventListener("click", onLogoutClick);

setAuthMode(state.mode);
updateSessionView();
logEvent("Session restored from localStorage.");

async function onAuthSubmit(event) {
    event.preventDefault();

    const payload = {
        email: ui.email.value.trim(),
        password: ui.password.value
    };

    const endpoint = state.mode === "register" ? "/auth/register" : "/auth/login";
    const actionLabel = state.mode === "register" ? "Registration" : "Login";

    setLoading(ui.authSubmit, true);
    try {
        const response = await apiRequest(endpoint, {
            method: "POST",
            body: JSON.stringify(payload)
        });

        storeTokens(response);
        setMessage(`${actionLabel} succeeded. Access and refresh tokens are saved.`, "success");
        logEvent(`${actionLabel} succeeded for ${payload.email}.`);
        updateSessionView();

        if (state.mode === "register") {
            setAuthMode("login");
        }
    } catch (error) {
        setMessage(error.message, "error");
        logEvent(`${actionLabel} failed: ${error.message}`);
    } finally {
        setLoading(ui.authSubmit, false);
    }
}

async function onPassengerSubmit(event) {
    event.preventDefault();

    if (!state.accessToken) {
        setMessage("Sign in first to get a JWT token.", "error");
        return;
    }

    setLoading(ui.passengerSubmit, true);
    try {
        const response = await apiRequest("/passengers", {
            method: "POST",
            headers: {
                Authorization: `Bearer ${state.accessToken}`
            },
            body: JSON.stringify({
                firstName: ui.firstName.value.trim(),
                lastName: ui.lastName.value.trim()
            })
        });

        state.passenger = response;
        localStorage.setItem(STORAGE_KEYS.passenger, JSON.stringify(response));
        setMessage(`Passenger ${response.firstName} ${response.lastName} created. Rating: ${response.rating}.`, "success");
        logEvent(`Passenger profile created for ${response.firstName} ${response.lastName}.`);
        updateSessionView();
        ui.passengerForm.reset();
    } catch (error) {
        setMessage(error.message, "error");
        logEvent(`Passenger creation failed: ${error.message}`);
    } finally {
        setLoading(ui.passengerSubmit, false);
    }
}

async function onRefreshClick() {
    if (!state.refreshToken) {
        setMessage("No refresh token found. Please login again.", "error");
        return;
    }

    setLoading(ui.refreshButton, true);
    try {
        const response = await apiRequest("/auth/refresh", {
            method: "POST",
            body: JSON.stringify({
                refreshToken: state.refreshToken
            })
        });

        storeTokens(response);
        setMessage("Access token refreshed successfully.", "success");
        logEvent("Access token refreshed successfully.");
        updateSessionView();
    } catch (error) {
        setMessage(error.message, "error");
        logEvent(`Refresh request failed: ${error.message}`);
    } finally {
        setLoading(ui.refreshButton, false);
    }
}

async function onLogoutClick() {
    if (!state.refreshToken) {
        clearSession();
        setMessage("Local session cleared.", "success");
        return;
    }

    setLoading(ui.logoutButton, true);
    try {
        await apiRequest("/auth/logout", {
            method: "POST",
            body: JSON.stringify({
                refreshToken: state.refreshToken
            })
        });

        logEvent("Logout completed on the server.");
    } catch (error) {
        logEvent(`Server logout failed: ${error.message}. Local session will still be cleared.`);
    } finally {
        clearSession();
        setMessage("Session closed and local tokens removed.", "success");
        setLoading(ui.logoutButton, false);
    }
}

function setAuthMode(mode) {
    state.mode = mode;
    const isRegister = mode === "register";

    ui.authTabs.forEach((button) => {
        button.classList.toggle("active", button.dataset.authMode === mode);
    });

    ui.authSubmit.textContent = isRegister ? "Register" : "Login";
    ui.authSubmit.dataset.originalLabel = ui.authSubmit.textContent;
    ui.authHint.textContent = isRegister
        ? "Registration sends email and password to POST /auth/register."
        : "Login sends email and password to POST /auth/login.";
}

function updateSessionView() {
    const payload = decodeJwt(state.accessToken);
    const userId = payload?.sub || "Will appear after login";
    const expiresAt = payload?.exp ? new Date(payload.exp * 1000).toLocaleString("en-GB") : "No data";
    const passengerText = state.passenger
        ? `${state.passenger.firstName} ${state.passenger.lastName} | rating ${state.passenger.rating}`
        : "Not created yet";
    const isAuthorized = Boolean(state.accessToken);

    ui.sessionTitle.textContent = isAuthorized ? "Session active" : "Not signed in";
    ui.sessionBadge.textContent = isAuthorized ? "online" : "offline";
    ui.sessionBadge.className = `badge ${isAuthorized ? "badge-online" : "badge-idle"}`;
    ui.userId.textContent = userId;
    ui.accessTokenPreview.textContent = isAuthorized ? shortenToken(state.accessToken) : "No token yet";
    ui.tokenExpiry.textContent = expiresAt;
    ui.passengerPreview.textContent = passengerText;
    ui.passengerLock.textContent = isAuthorized ? "JWT ready" : "JWT required";
    ui.passengerLock.className = `badge ${isAuthorized ? "badge-online" : "badge-idle"}`;
    ui.passengerSubmit.disabled = !isAuthorized;
    ui.refreshButton.disabled = !state.refreshToken;
    ui.logoutButton.disabled = !isAuthorized && !state.refreshToken;
}

async function apiRequest(path, options = {}) {
    const response = await fetch(path, {
        method: options.method || "GET",
        headers: {
            "Content-Type": "application/json",
            ...(options.headers || {})
        },
        body: options.body
    });

    const contentType = response.headers.get("content-type") || "";
    const payload = contentType.includes("application/json")
        ? await response.json().catch(() => null)
        : await response.text().catch(() => "");

    if (!response.ok) {
        throw new Error(extractErrorMessage(payload, response.status));
    }

    return payload;
}

function extractErrorMessage(payload, status) {
    if (!payload) {
        return `Request failed (${status}).`;
    }

    if (typeof payload === "string") {
        return payload || `Request failed (${status}).`;
    }

    return payload.message
        || payload.error
        || payload.detail
        || payload.title
        || `Request failed (${status}).`;
}

function storeTokens(tokens) {
    state.accessToken = tokens.accessToken || "";
    state.refreshToken = tokens.refreshToken || "";

    localStorage.setItem(STORAGE_KEYS.accessToken, state.accessToken);
    localStorage.setItem(STORAGE_KEYS.refreshToken, state.refreshToken);
}

function clearSession() {
    state.accessToken = "";
    state.refreshToken = "";
    state.passenger = null;

    localStorage.removeItem(STORAGE_KEYS.accessToken);
    localStorage.removeItem(STORAGE_KEYS.refreshToken);
    localStorage.removeItem(STORAGE_KEYS.passenger);
    updateSessionView();
}

function setMessage(message, type) {
    ui.messageBox.textContent = message;
    ui.messageBox.className = `message-box ${type || ""}`.trim();
}

function setLoading(button, isLoading) {
    button.disabled = isLoading;
    button.dataset.originalLabel = button.dataset.originalLabel || button.textContent;
    button.textContent = isLoading ? "Please wait..." : button.dataset.originalLabel;
}

function logEvent(text) {
    const item = document.createElement("li");
    item.textContent = `${new Date().toLocaleTimeString("en-GB")}: ${text}`;
    ui.eventFeed.prepend(item);

    while (ui.eventFeed.children.length > 6) {
        ui.eventFeed.removeChild(ui.eventFeed.lastChild);
    }
}

function shortenToken(token) {
    if (token.length <= 28) {
        return token;
    }

    return `${token.slice(0, 18)}...${token.slice(-10)}`;
}

function decodeJwt(token) {
    if (!token || !token.includes(".")) {
        return null;
    }

    try {
        const base64 = token.split(".")[1]
            .replace(/-/g, "+")
            .replace(/_/g, "/");

        const decoded = decodeURIComponent(
            atob(base64)
                .split("")
                .map((char) => `%${(`00${char.charCodeAt(0).toString(16)}`).slice(-2)}`)
                .join("")
        );

        return JSON.parse(decoded);
    } catch (error) {
        return null;
    }
}

function readStoredJson(key) {
    const value = localStorage.getItem(key);
    if (!value) {
        return null;
    }

    try {
        return JSON.parse(value);
    } catch (error) {
        localStorage.removeItem(key);
        return null;
    }
}

