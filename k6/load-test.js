import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
    stages: [
        { duration: '30s', target: 5 },
        { duration: '1m', target: 20 },
        { duration: '30s', target: 0 },
    ],
    thresholds: {
        http_req_failed: ['rate<0.05'],
        http_req_duration: ['p(95)<500'],
    },
};

const BASE_URL = 'http://localhost:8080';
const users = Array.from({ length: 20 }, (_, i) => ({
    email: `loadtest${i + 1}@test.com`,
    password: 'secret123',
}));

export default function () {
    const user = users[(__VU - 1) % users.length];

    const res = http.post(
        `${BASE_URL}/auth/login`,
        JSON.stringify({
            email: user.email,
            password: user.password,
        }),
        {
            headers: { 'Content-Type': 'application/json' },
            tags: { endpoint: 'login' },
        }
    );

    check(res, {
        'login status 200': (r) => r.status === 200,
    });

    sleep(1);
}
