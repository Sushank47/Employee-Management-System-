const BASE_URL = ''; // Proxied through Vite server configuration

const getHeaders = () => {
  const token = localStorage.getItem('token');
  const headers = {
    'Content-Type': 'application/json',
  };
  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }
  return headers;
};

const handleResponse = async (response) => {
  if (!response.ok) {
    const errorData = await response.json().catch(() => ({}));
    const errorMessage = errorData.message || `HTTP error! status: ${response.status}`;
    throw new Error(errorMessage);
  }
  if (response.status === 204) {
    return null;
  }
  return response.json();
};

export const api = {
  // Authentication
  login: async (email, password) => {
    const res = await fetch(`${BASE_URL}/api/auth/login`, {
      method: 'POST',
      headers: getHeaders(),
      body: JSON.stringify({ username: email, password }),
    });
    const data = await handleResponse(res);
    if (data && data.token) {
      localStorage.setItem('token', data.token);
      
      // Decode JWT token loosely to extract username & roles
      try {
        const payload = JSON.parse(atob(data.token.split('.')[1]));
        localStorage.setItem('user', JSON.stringify({
          email: payload.sub,
          roles: payload.roles || [],
          employeeId: payload.employeeId || ''
        }));
      } catch (e) {
        console.error('Failed to parse JWT payload', e);
      }
    }
    return data;
  },

  register: async (email, password, employeeId, roles) => {
    const res = await fetch(`${BASE_URL}/api/auth/register`, {
      method: 'POST',
      headers: getHeaders(),
      body: JSON.stringify({ email, password, employeeId, roles }),
    });
    return handleResponse(res);
  },

  logout: () => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
  },

  // Dashboard / Analytics
  getDashboardOverview: async () => {
    const res = await fetch(`${BASE_URL}/api/dashboard/overview`, {
      method: 'GET',
      headers: getHeaders(),
    });
    return handleResponse(res);
  },

  // Employee Management
  getEmployees: async (page = 0, size = 10, sort = 'employeeId') => {
    const res = await fetch(`${BASE_URL}/api/employees?page=${page}&size=${size}&sort=${sort}`, {
      method: 'GET',
      headers: getHeaders(),
    });
    return handleResponse(res);
  },

  searchEmployees: async (params, page = 0, size = 10, sort = 'employeeId') => {
    const queryParams = new URLSearchParams({ page, size, sort });
    if (params.name) queryParams.append('name', params.name);
    if (params.email) queryParams.append('email', params.email);
    if (params.departmentId) queryParams.append('departmentId', params.departmentId);
    if (params.skill) queryParams.append('skill', params.skill);
    if (params.designation) queryParams.append('designation', params.designation);

    const res = await fetch(`${BASE_URL}/api/employees/search?${queryParams.toString()}`, {
      method: 'GET',
      headers: getHeaders(),
    });
    return handleResponse(res);
  },

  createEmployee: async (employeeData) => {
    const res = await fetch(`${BASE_URL}/api/employees`, {
      method: 'POST',
      headers: getHeaders(),
      body: JSON.stringify(employeeData),
    });
    return handleResponse(res);
  },

  updateEmployee: async (employeeId, employeeData) => {
    const res = await fetch(`${BASE_URL}/api/employees/${employeeId}`, {
      method: 'PUT',
      headers: getHeaders(),
      body: JSON.stringify(employeeData),
    });
    return handleResponse(res);
  },

  deleteEmployee: async (employeeId) => {
    const res = await fetch(`${BASE_URL}/api/employees/${employeeId}`, {
      method: 'DELETE',
      headers: getHeaders(),
    });
    return handleResponse(res);
  },

  // Leave Management
  applyLeave: async (employeeId, startDate, endDate, reason) => {
    const res = await fetch(`${BASE_URL}/api/leaves/apply`, {
      method: 'POST',
      headers: getHeaders(),
      body: JSON.stringify({ employeeId, startDate, endDate, reason }),
    });
    return handleResponse(res);
  },

  getLeaves: async () => {
    const res = await fetch(`${BASE_URL}/api/leaves`, {
      method: 'GET',
      headers: getHeaders(),
    });
    return handleResponse(res);
  },

  approveLeave: async (leaveId) => {
    const res = await fetch(`${BASE_URL}/api/leaves/${leaveId}/approve`, {
      method: 'POST',
      headers: getHeaders(),
    });
    return handleResponse(res);
  },

  rejectLeave: async (leaveId) => {
    const res = await fetch(`${BASE_URL}/api/leaves/${leaveId}/reject`, {
      method: 'POST',
      headers: getHeaders(),
    });
    return handleResponse(res);
  },

  // Attendance Management
  checkIn: async (employeeId) => {
    const res = await fetch(`${BASE_URL}/api/attendance/checkin`, {
      method: 'POST',
      headers: getHeaders(),
      body: JSON.stringify({ employeeId }),
    });
    return handleResponse(res);
  },

  checkOut: async (employeeId) => {
    const res = await fetch(`${BASE_URL}/api/attendance/checkout`, {
      method: 'POST',
      headers: getHeaders(),
      body: JSON.stringify({ employeeId }),
    });
    return handleResponse(res);
  },

  // Notifications
  getNotifications: async (employeeId) => {
    const res = await fetch(`${BASE_URL}/api/notifications/employee/${employeeId}`, {
      method: 'GET',
      headers: getHeaders(),
    });
    return handleResponse(res);
  },

  getUnreadNotifications: async (employeeId) => {
    const res = await fetch(`${BASE_URL}/api/notifications/employee/${employeeId}/unread`, {
      method: 'GET',
      headers: getHeaders(),
    });
    return handleResponse(res);
  },

  markNotificationAsRead: async (notificationId) => {
    const res = await fetch(`${BASE_URL}/api/notifications/${notificationId}/read`, {
      method: 'PUT',
      headers: getHeaders(),
    });
    return handleResponse(res);
  },

  markAllNotificationsAsRead: async (employeeId) => {
    const res = await fetch(`${BASE_URL}/api/notifications/employee/${employeeId}/read-all`, {
      method: 'PUT',
      headers: getHeaders(),
    });
    return handleResponse(res);
  }
};
