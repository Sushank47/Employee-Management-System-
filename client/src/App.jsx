import { useState, useEffect } from 'react';
import { 
  Sparkles, Download, Wand2, BookOpen, ArrowRight, 
  Menu, Users, Calendar, Clock, LogOut, Trash2, Edit, Plus, 
  Search, Briefcase, ChevronLeft, ChevronRight, X, Check,
  Bell, Eye, EyeOff
} from 'lucide-react';
import { api } from './services/api';
import './App.css';

const TwitterIcon = (props) => (
  <svg viewBox="0 0 24 24" width="24" height="24" stroke="currentColor" strokeWidth="2" fill="none" strokeLinecap="round" strokeLinejoin="round" {...props}>
    <path d="M22 4s-.7 2.1-2 3.4c1.6 10-9.4 17.3-18 11.6 2.2.1 4.4-.6 6-2C3 15.5.5 9.6 3 5c2.2 2.6 5.6 4.1 9 4-.9-4.2 4-6.6 7-3.8 1.1 0 3-1.2 3-1.2z"></path>
  </svg>
);

const LinkedinIcon = (props) => (
  <svg viewBox="0 0 24 24" width="24" height="24" stroke="currentColor" strokeWidth="2" fill="none" strokeLinecap="round" strokeLinejoin="round" {...props}>
    <path d="M16 8a6 6 0 0 1 6 6v7h-4v-7a2 2 0 0 0-2-2 2 2 0 0 0-2 2v7h-4v-7a6 6 0 0 1 6-6z"></path>
    <rect x="2" y="9" width="4" height="12"></rect>
    <circle cx="4" cy="4" r="2"></circle>
  </svg>
);

const InstagramIcon = (props) => (
  <svg viewBox="0 0 24 24" width="24" height="24" stroke="currentColor" strokeWidth="2" fill="none" strokeLinecap="round" strokeLinejoin="round" {...props}>
    <rect x="2" y="2" width="20" height="20" rx="5" ry="5"></rect>
    <path d="M16 11.37A4 4 0 1 1 12.63 8 4 4 0 0 1 16 11.37z"></path>
    <line x1="17.5" y1="6.5" x2="17.51" y2="6.5"></line>
  </svg>
);

function App() {
  const [isAuthenticated, setIsAuthenticated] = useState(!!localStorage.getItem('token'));
  const [user, setUser] = useState(() => {
    const saved = localStorage.getItem('user');
    return saved ? JSON.parse(saved) : null;
  });

  // Navigation and active view states
  const [activeTab, setActiveTab] = useState('dashboard'); // 'dashboard', 'employees', 'leaves', 'attendance'
  const [menuOpen, setMenuOpen] = useState(false);

  // Auth States
  const [landingTab, setLandingTab] = useState('home'); // 'home', 'guiding', 'organisation', 'login'
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [showRegPassword, setShowRegPassword] = useState(false);
  const [isRegistering, setIsRegistering] = useState(false);
  const [regEmployeeId, setRegEmployeeId] = useState('');
  const [regRole, setRegRole] = useState('EMPLOYEE');
  const [authError, setAuthError] = useState('');
  const [authSuccess, setAuthSuccess] = useState('');

  // Notifications State
  const [notifications, setNotifications] = useState([]);
  const [showNotificationsDropdown, setShowNotificationsDropdown] = useState(false);

  // Dashboard Data
  const [dashboard, setDashboard] = useState(null);
  const [dashboardLoading, setDashboardLoading] = useState(false);

  // Employee Directory States
  const [employees, setEmployees] = useState([]);
  const [employeePage, setEmployeePage] = useState(0);
  const [employeeTotalPages, setEmployeeTotalPages] = useState(0);
  const [employeeSort, setEmployeeSort] = useState('employeeId');
  const [employeeSearch, setEmployeeSearch] = useState({
    name: '',
    email: '',
    departmentId: '',
    skill: '',
    designation: ''
  });
  const [showEmployeeModal, setShowEmployeeModal] = useState(false);
  const [editingEmployee, setEditingEmployee] = useState(null);
  const [employeeForm, setEmployeeForm] = useState({
    employeeId: '',
    firstName: '',
    lastName: '',
    email: '',
    phone: '',
    departmentId: '',
    managerId: '',
    designation: '',
    salary: 0,
    skills: ''
  });

  // Leave Management States
  const [leaves, setLeaves] = useState([]);
  const [leaveEmpId, setLeaveEmpId] = useState('');
  const [leaveStartDate, setLeaveStartDate] = useState('');
  const [leaveEndDate, setLeaveEndDate] = useState('');
  const [leaveReason, setLeaveReason] = useState('');
  const [leaveError, setLeaveError] = useState('');
  const [leaveSuccess, setLeaveSuccess] = useState('');

  // Attendance States
  const [attEmpId, setAttEmpId] = useState('');
  const [attMessage, setAttMessage] = useState('');
  const [attError, setAttError] = useState('');

  const isStaff = user?.roles?.some(role => ['ROLE_ADMIN', 'ROLE_HR', 'ROLE_MANAGER'].includes(role));
  const isManagerOrAdmin = user?.roles?.some(role => ['ROLE_MANAGER', 'ROLE_ADMIN'].includes(role));

  const fetchNotifications = async () => {
    if (user?.employeeId) {
      try {
        const data = await api.getNotifications(user.employeeId);
        setNotifications(data || []);
      } catch (e) {
        console.error("Error fetching notifications", e);
      }
    }
  };

  const handleMarkNotificationRead = async (id) => {
    try {
      await api.markNotificationAsRead(id);
      fetchNotifications();
    } catch (e) {
      console.error("Error marking notification as read", e);
    }
  };

  const handleMarkAllNotificationsRead = async () => {
    if (user?.employeeId) {
      try {
        await api.markAllNotificationsAsRead(user.employeeId);
        fetchNotifications();
      } catch (e) {
        console.error("Error marking all notifications as read", e);
      }
    }
  };

  // Auto load dashboard data
  useEffect(() => {
    if (isAuthenticated) {
      // Direct security check
      if (activeTab === 'employees' && !isStaff) {
        setActiveTab('dashboard');
        return;
      }

      fetchDashboardData();
      fetchNotifications();
      if (activeTab === 'employees') {
        fetchEmployees();
      } else if (activeTab === 'leaves') {
        fetchLeaves();
      }
    }
  }, [isAuthenticated, activeTab, employeePage, employeeSort]);

  // Poll for notifications
  useEffect(() => {
    if (isAuthenticated && user?.employeeId) {
      const interval = setInterval(() => {
        fetchNotifications();
      }, 10000);
      return () => clearInterval(interval);
    }
  }, [isAuthenticated, user]);

  // Pre-fill Employee ID fields for form defaults
  useEffect(() => {
    if (isAuthenticated && user?.employeeId) {
      setLeaveEmpId(user.employeeId);
      setAttEmpId(user.employeeId);
    }
  }, [isAuthenticated, user]);

  const fetchDashboardData = async () => {
    setDashboardLoading(true);
    try {
      const data = await api.getDashboardOverview();
      setDashboard(data);
    } catch (e) {
      console.error("Error fetching dashboard overview", e);
    } finally {
      setDashboardLoading(false);
    }
  };

  const fetchEmployees = async () => {
    try {
      // Check if searching
      const isSearching = Object.values(employeeSearch).some(val => val.trim() !== '');
      let data;
      if (isSearching) {
        data = await api.searchEmployees(employeeSearch, employeePage, 8, employeeSort);
      } else {
        data = await api.getEmployees(employeePage, 8, employeeSort);
      }
      setEmployees(data.content || []);
      setEmployeeTotalPages(data.totalPages || 0);
    } catch (e) {
      console.error("Error fetching employees", e);
    }
  };

  const fetchLeaves = async () => {
    try {
      const data = await api.getLeaves();
      setLeaves(data || []);
    } catch (e) {
      console.error("Error fetching leaves", e);
    }
  };

  // Auth Handlers
  const handleLogin = async (e) => {
    e.preventDefault();
    setAuthError('');
    try {
      const data = await api.login(email, password);
      setIsAuthenticated(true);
      const savedUser = localStorage.getItem('user');
      setUser(savedUser ? JSON.parse(savedUser) : null);
      setActiveTab('dashboard');
    } catch (err) {
      setAuthError(err.message || 'Login failed. Please check credentials.');
    }
  };

  const handleRegister = async (e) => {
    e.preventDefault();
    setAuthError('');
    setAuthSuccess('');
    try {
      await api.register(email, password, regEmployeeId, [regRole]);
      setAuthSuccess('Registration successful! Please login.');
      setIsRegistering(false);
    } catch (err) {
      setAuthError(err.message || 'Registration failed.');
    }
  };

  const handleLogout = () => {
    api.logout();
    setIsAuthenticated(false);
    setUser(null);
    setDashboard(null);
    setEmployees([]);
    setLeaves([]);
  };

  // Employee CRUD Handlers
  const handleSaveEmployee = async (e) => {
    e.preventDefault();
    try {
      const skillsArray = employeeForm.skills
        ? employeeForm.skills.split(',').map(s => s.trim()).filter(Boolean)
        : [];
      
      const payload = {
        ...employeeForm,
        skills: skillsArray,
        salary: Number(employeeForm.salary)
      };

      if (editingEmployee) {
        await api.updateEmployee(editingEmployee.employeeId, payload);
      } else {
        await api.createEmployee(payload);
      }

      setShowEmployeeModal(false);
      setEditingEmployee(null);
      fetchEmployees();
      fetchDashboardData();
    } catch (err) {
      alert(err.message || 'Failed to save employee.');
    }
  };

  const handleDeleteEmployee = async (empId) => {
    if (window.confirm(`Are you sure you want to delete employee ${empId}?`)) {
      try {
        await api.deleteEmployee(empId);
        fetchEmployees();
        fetchDashboardData();
      } catch (err) {
        alert(err.message || 'Failed to delete employee.');
      }
    }
  };

  const openAddEmployeeModal = () => {
    setEditingEmployee(null);
    setEmployeeForm({
      employeeId: '',
      firstName: '',
      lastName: '',
      email: '',
      phone: '',
      departmentId: '',
      managerId: '',
      designation: '',
      salary: 0,
      skills: ''
    });
    setShowEmployeeModal(true);
  };

  const openEditEmployeeModal = (emp) => {
    setEditingEmployee(emp);
    setEmployeeForm({
      employeeId: emp.employeeId,
      firstName: emp.firstName,
      lastName: emp.lastName,
      email: emp.email,
      phone: emp.phone || '',
      departmentId: emp.departmentId || '',
      managerId: emp.managerId || '',
      designation: emp.designation || '',
      salary: emp.salary || 0,
      skills: emp.skills ? emp.skills.join(', ') : ''
    });
    setShowEmployeeModal(true);
  };

  // Leave Handlers
  const handleApplyLeave = async (e) => {
    e.preventDefault();
    setLeaveError('');
    setLeaveSuccess('');
    try {
      await api.applyLeave(leaveEmpId, leaveStartDate, leaveEndDate, leaveReason);
      setLeaveSuccess('Leave application submitted successfully!');
      setLeaveStartDate('');
      setLeaveEndDate('');
      setLeaveReason('');
      fetchLeaves();
      fetchDashboardData();
      fetchNotifications();
    } catch (err) {
      setLeaveError(err.message || 'Failed to apply leave.');
    }
  };

  const handleApproveLeave = async (leaveId) => {
    try {
      await api.approveLeave(leaveId);
      fetchLeaves();
      fetchDashboardData();
      fetchNotifications();
    } catch (err) {
      alert(err.message || 'Failed to approve leave.');
    }
  };

  const handleRejectLeave = async (leaveId) => {
    try {
      await api.rejectLeave(leaveId);
      fetchLeaves();
      fetchDashboardData();
      fetchNotifications();
    } catch (err) {
      alert(err.message || 'Failed to reject leave.');
    }
  };

  // Attendance Handlers
  const handleCheckIn = async (e) => {
    e.preventDefault();
    setAttError('');
    setAttMessage('');
    try {
      const data = await api.checkIn(attEmpId);
      setAttMessage(`Successfully Checked-In at ${new Date(data.checkInTime).toLocaleTimeString()}`);
      fetchDashboardData();
      fetchNotifications();
    } catch (err) {
      setAttError(err.message || 'Check-in failed.');
    }
  };

  const handleCheckOut = async (e) => {
    e.preventDefault();
    setAttError('');
    setAttMessage('');
    try {
      const data = await api.checkOut(attEmpId);
      setAttMessage(`Successfully Checked-Out at ${new Date(data.checkOutTime).toLocaleTimeString()}`);
      fetchDashboardData();
      fetchNotifications();
    } catch (err) {
      setAttError(err.message || 'Check-out failed.');
    }
  };

  return (
    <div className="relative min-h-screen w-full flex flex-col justify-between overflow-hidden">
      {/* Looping video background */}
      <div className="bg-video-container">
        <video autoPlay loop muted playsInline>
          <source 
            src="https://d8j0ntlcm91z4.cloudfront.net/user_38xzZboKViGWJOttwIXH07lWA1P/hf_20260315_073750_51473149-4350-4920-ae24-c8214286f323.mp4" 
            type="video/mp4" 
          />
        </video>
        {/* Dark overlay for readability */}
        <div className="absolute inset-0 bg-black/65 z-0" />
      </div>

      {!isAuthenticated ? (
        // --- NEW PREMIUM FULL SCREEN LANDING LAYOUT (NO BOX container, only in login) ---
        <div className="relative z-10 flex flex-col min-h-screen w-full">
          {/* Premium Ambient Background Glows */}
          <div className="ambient-glow-1" />
          <div className="ambient-glow-2" />
          
          {/* Header Navigation */}
          <header className="relative z-20 w-full flex flex-col sm:flex-row items-center justify-between px-6 py-4 border-b border-white/5 gap-4">
            <div className="flex items-center gap-2">
              <div className="w-8 h-8 rounded-full bg-white/10 flex items-center justify-center liquid-glass">
                <Sparkles className="w-4 h-4 text-white" />
              </div>
              <span className="font-semibold text-xl tracking-tighter text-white">bloom <em className="text-white/60">ems</em></span>
            </div>
            
            {/* Center Navigation Links */}
            <div className="flex items-center gap-6 bg-white/5 p-1 rounded-full border border-white/5 liquid-glass px-4 py-1.5">
              <button 
                onClick={() => { setLandingTab('home'); setIsRegistering(false); }}
                className={`text-xs font-semibold transition-colors interactive-item ${landingTab === 'home' ? 'text-white' : 'text-white/50 hover:text-white'}`}
              >
                Home
              </button>
              <button 
                onClick={() => { setLandingTab('guiding'); setIsRegistering(false); }}
                className={`text-xs font-semibold transition-colors interactive-item ${landingTab === 'guiding' ? 'text-white' : 'text-white/50 hover:text-white'}`}
              >
                Guiding
              </button>
              <button 
                onClick={() => { setLandingTab('organisation'); setIsRegistering(false); }}
                className={`text-xs font-semibold transition-colors interactive-item ${landingTab === 'organisation' ? 'text-white' : 'text-white/50 hover:text-white'}`}
              >
                Organisation
              </button>
            </div>

            {/* Right Side Socials & Sign In */}
            <div className="flex items-center gap-4">
              <div className="flex items-center gap-2 bg-white/5 px-3 py-1.5 rounded-full liquid-glass">
                <a href="https://x.com" target="_blank" rel="noopener noreferrer" className="w-7 h-7 rounded-full bg-white/5 hover:bg-white/10 flex items-center justify-center text-white/80 hover:text-white transition-all interactive-item">
                  <TwitterIcon className="w-3.5 h-3.5" />
                </a>
                <a href="https://in.linkedin.com/" target="_blank" rel="noopener noreferrer" className="w-7 h-7 rounded-full bg-white/5 hover:bg-white/10 flex items-center justify-center text-white/80 hover:text-white transition-all interactive-item">
                  <LinkedinIcon className="w-3.5 h-3.5" />
                </a>
                <a href="https://www.instagram.com" target="_blank" rel="noopener noreferrer" className="w-7 h-7 rounded-full bg-white/5 hover:bg-white/10 flex items-center justify-center text-white/80 hover:text-white transition-all interactive-item">
                  <InstagramIcon className="w-3.5 h-3.5" />
                </a>
              </div>
              <button 
                onClick={() => setLandingTab('login')} 
                className={`glass-btn text-xs px-4 py-2 font-semibold interactive-item ${landingTab === 'login' ? 'glass-btn-primary' : ''}`}
              >
                Sign In
              </button>
            </div>
          </header>

          {/* Landing Content Areas */}
          {landingTab === 'home' && (
            <div className="flex-1 flex flex-col items-center justify-center text-center p-6 max-w-4xl mx-auto z-10 my-auto">
              {/* Premium Top Badge */}
              <div className="flex items-center gap-1.5 bg-white/5 border border-white/10 px-3 py-1 rounded-full text-[10px] tracking-wider uppercase font-semibold text-white/70 mb-6 liquid-glass">
                <span className="w-1.5 h-1.5 rounded-full bg-white animate-pulse" />
                ⚡ Next-Gen Workforce Analytics AI
              </div>

              <h1 className="text-4xl lg:text-5xl font-medium tracking-tight leading-tight mb-4 gradient-text">
                Enterprise-grade <br />
                <span className="italic font-serif">workforce intelligence</span> platform
              </h1>
              <p className="text-sm text-white/70 max-w-2xl mx-auto mb-8 leading-relaxed">
                Bloom EMS provides seamless automation for your organization. Clock attendance, request leaves with automated engine check limits, track team performance parameters, and calculate monthly payroll balances instantly.
              </p>
              
              {/* Metrics Preview Row */}
              <div className="grid grid-cols-1 sm:grid-cols-3 gap-4 w-full max-w-lg mb-8">
                <div className="landing-card liquid-glass p-3 rounded-xl border border-white/5 text-center">
                  <span className="text-white/40 text-[9px] uppercase tracking-wider block">System SLA</span>
                  <span className="text-lg font-bold text-white mt-0.5">99.98%</span>
                </div>
                <div className="landing-card liquid-glass p-3 rounded-xl border border-white/5 text-center">
                  <span className="text-white/40 text-[9px] uppercase tracking-wider block">Sync Speed</span>
                  <span className="text-lg font-bold text-white mt-0.5">&lt; 15ms</span>
                </div>
                <div className="landing-card liquid-glass p-3 rounded-xl border border-white/5 text-center">
                  <span className="text-white/40 text-[9px] uppercase tracking-wider block">Active Shifts</span>
                  <span className="text-lg font-bold text-white mt-0.5">14.8k+</span>
                </div>
              </div>
              
              {/* Expanded 4-Feature Cards Grid */}
              <div className="grid grid-cols-1 md:grid-cols-2 gap-6 w-full mb-8">
                <div className="landing-card liquid-glass-strong p-6 rounded-2xl text-left border border-white/5 flex items-start gap-4">
                  <div className="w-10 h-10 rounded-xl bg-white/5 border border-white/10 flex items-center justify-center shrink-0">
                    <Clock className="w-5 h-5 text-white" />
                  </div>
                  <div>
                    <h3 className="text-sm font-bold uppercase tracking-wider text-white mb-1">Smart Attendance Shift</h3>
                    <p className="text-xs text-white/60 leading-relaxed">Secure location-based checking in/out logs configured autonomously with instant notification broadcasts.</p>
                  </div>
                </div>

                <div className="landing-card liquid-glass-strong p-6 rounded-2xl text-left border border-white/5 flex items-start gap-4">
                  <div className="w-10 h-10 rounded-xl bg-white/5 border border-white/10 flex items-center justify-center shrink-0">
                    <Calendar className="w-5 h-5 text-white" />
                  </div>
                  <div>
                    <h3 className="text-sm font-bold uppercase tracking-wider text-white mb-1">Auto Leave Engine</h3>
                    <p className="text-xs text-white/60 leading-relaxed">Automated rules engine: leave requests &le; 2 days auto-approved; department capacity overloads auto-rejected.</p>
                  </div>
                </div>

                <div className="landing-card liquid-glass-strong p-6 rounded-2xl text-left border border-white/5 flex items-start gap-4">
                  <div className="w-10 h-10 rounded-xl bg-white/5 border border-white/10 flex items-center justify-center shrink-0">
                    <Users className="w-5 h-5 text-white" />
                  </div>
                  <div>
                    <h3 className="text-sm font-bold uppercase tracking-wider text-white mb-1">Role Security Access</h3>
                    <p className="text-xs text-white/60 leading-relaxed">Granular role restrictions: prevent cross-employee manipulation and isolate approval flows to managers.</p>
                  </div>
                </div>

                <div className="landing-card liquid-glass-strong p-6 rounded-2xl text-left border border-white/5 flex items-start gap-4">
                  <div className="w-10 h-10 rounded-xl bg-white/5 border border-white/10 flex items-center justify-center shrink-0">
                    <Bell className="w-5 h-5 text-white" />
                  </div>
                  <div>
                    <h3 className="text-sm font-bold uppercase tracking-wider text-white mb-1">Live Notifications</h3>
                    <p className="text-xs text-white/60 leading-relaxed">Real-time status tracking for leave submissions, manager decisions, and clock-in/out updates.</p>
                  </div>
                </div>
              </div>
              
              {/* Cohesive Primary/Secondary CTAs */}
              <div className="flex flex-col sm:flex-row gap-3 items-center justify-center w-full">
                <button 
                  onClick={() => setLandingTab('login')} 
                  className="glass-btn glass-btn-primary px-8 py-3 rounded-xl font-semibold flex items-center justify-center gap-2 interactive-item text-xs w-full sm:w-auto"
                >
                  Access workforce portal <ArrowRight className="w-4 h-4" />
                </button>
                <button 
                  onClick={() => setLandingTab('guiding')} 
                  className="glass-btn px-8 py-3 rounded-xl font-semibold flex items-center justify-center gap-2 interactive-item text-xs w-full sm:w-auto bg-white/5 hover:bg-white/10 border border-white/5"
                >
                  View system manual
                </button>
              </div>
            </div>
          )}

          {landingTab === 'guiding' && (
            <div className="flex-1 flex flex-col items-center justify-center p-6 max-w-4xl mx-auto z-10 my-auto">
              <div className="text-center mb-8">
                <div className="w-16 h-16 rounded-full bg-white/10 flex items-center justify-center liquid-glass mx-auto mb-4">
                  <BookOpen className="w-8 h-8 text-white" />
                </div>
                <h2 className="text-3xl font-semibold tracking-tight text-white">System Guide &amp; User Manual</h2>
                <p className="text-sm text-white/50 mt-1">Understanding role rights, attendance tracking, and leaves flow</p>
              </div>

              <div className="grid grid-cols-1 md:grid-cols-3 gap-6 w-full">
                <div className="liquid-glass-strong p-5 rounded-2xl border border-white/5 text-left">
                  <span className="text-3xl font-serif text-white/40 block mb-2">01</span>
                  <h4 className="text-sm font-bold text-white uppercase tracking-wider mb-2">Registration</h4>
                  <p className="text-xs text-white/60 leading-relaxed">
                    Register using corporate email, password, and assigned Employee ID. Select standard Employee, Manager, HR Specialist, or System Admin role options.
                  </p>
                </div>
                <div className="liquid-glass-strong p-5 rounded-2xl border border-white/5 text-left">
                  <span className="text-3xl font-serif text-white/40 block mb-2">02</span>
                  <h4 className="text-sm font-bold text-white uppercase tracking-wider mb-2">Shift Logging</h4>
                  <p className="text-xs text-white/60 leading-relaxed">
                    Standard Employees can check-in/out only for their own fixed ID. Staff roles (HR/Managers/Admins) have master rights to check-in/out for any employee ID.
                  </p>
                </div>
                <div className="liquid-glass-strong p-5 rounded-2xl border border-white/5 text-left">
                  <span className="text-3xl font-serif text-white/40 block mb-2">03</span>
                  <h4 className="text-sm font-bold text-white uppercase tracking-wider mb-2">Leave Engine</h4>
                  <p className="text-xs text-white/60 leading-relaxed">
                    Auto-approves leave requests of 1-2 days. Auto-rejects requests when over 30% of department is already on leave. Manual approvals are restricted to Managers/Admins.
                  </p>
                </div>
              </div>
            </div>
          )}

          {landingTab === 'organisation' && (
            <div className="flex-1 flex flex-col items-center justify-center p-6 max-w-2xl mx-auto z-10 my-auto">
              <div className="text-center mb-8">
                <div className="w-16 h-16 rounded-full bg-white/10 flex items-center justify-center liquid-glass mx-auto mb-4">
                  <Users className="w-8 h-8 text-white" />
                </div>
                <h2 className="text-3xl font-semibold tracking-tight text-white">Our Organisation Framework</h2>
                <p className="text-sm text-white/50 mt-1">Driving transparency, intelligence, and growth autonomously</p>
              </div>

              <div className="liquid-glass-strong p-6 rounded-3xl border border-white/5 w-full flex flex-col gap-4 text-left">
                <div className="flex justify-between items-center pb-3 border-b border-white/5 text-sm">
                  <span className="text-white/60">Core Strategy</span>
                  <span className="font-semibold text-white">Workforce Analytics AI</span>
                </div>
                <div className="flex justify-between items-center pb-3 border-b border-white/5 text-sm">
                  <span className="text-white/60">System Database</span>
                  <span className="font-semibold text-white">MongoDB Relational Schema</span>
                </div>
                <div className="flex justify-between items-center text-sm">
                  <span className="text-white/60">Corporate Ethics</span>
                  <span className="font-semibold text-white">System Transparency &amp; Auditing</span>
                </div>
              </div>

              <div className="text-center mt-8">
                <span className="text-xs tracking-widest uppercase text-white/40 block mb-2">Corporate Mandate</span>
                <p className="text-sm text-white/70 italic font-serif max-w-lg mx-auto">
                  "Connecting teams, enabling intelligence, and building the future of enterprise resource tracking autonomously."
                </p>
              </div>
            </div>
          )}

          {landingTab === 'login' && (
            <div className="flex-1 flex items-center justify-center p-6 z-10 my-auto">
              <div className="w-full max-w-md liquid-glass-strong p-8 rounded-[2rem] border border-white/10 text-center relative animate-fade-in">
                <div className="text-center mb-6">
                  <div className="w-14 h-14 rounded-full bg-white/10 flex items-center justify-center liquid-glass mx-auto mb-4">
                    <Users className="w-6 h-6 text-white" />
                  </div>
                  <h2 className="text-2xl font-semibold tracking-tight text-white">
                    {isRegistering ? 'Create System Account' : 'Workforce AI Portal'}
                  </h2>
                  <p className="text-xs text-white/50 mt-1">
                    {isRegistering ? 'Sign up to request system dashboard access' : 'Enter credentials to authorize access'}
                  </p>
                </div>

                <form onSubmit={isRegistering ? handleRegister : handleLogin} className="flex flex-col gap-4 text-left">
                  {authError && (
                    <div className="text-xs text-white bg-black/50 border border-white/20 p-2.5 rounded-lg text-center font-medium">
                      {authError}
                    </div>
                  )}
                  {authSuccess && (
                    <div className="text-xs text-white bg-white/10 border border-white/20 p-2.5 rounded-lg text-center font-medium">
                      {authSuccess}
                    </div>
                  )}
                  
                  <div>
                    <label className="glass-label">Workplace Email</label>
                    <input 
                      type="email" 
                      required
                      className="glass-input" 
                      placeholder="name@company.com" 
                      value={email}
                      onChange={(e) => setEmail(e.target.value)}
                    />
                  </div>

                  <div>
                    <label className="glass-label">Password</label>
                    <div className="relative w-full flex items-center">
                      <input 
                        type={showPassword ? 'text' : 'password'} 
                        required
                        className="glass-input pr-10" 
                        placeholder="••••••••" 
                        value={password}
                        onChange={(e) => setPassword(e.target.value)}
                      />
                      <button
                        type="button"
                        onClick={() => setShowPassword(!showPassword)}
                        className="absolute right-3 text-white/50 hover:text-white flex items-center justify-center interactive-item"
                      >
                        {showPassword ? <EyeOff className="w-4 h-4" /> : <Eye className="w-4 h-4" />}
                      </button>
                    </div>
                  </div>

                  {isRegistering && (
                    <>
                      <div>
                        <label className="glass-label">Assigned Employee ID</label>
                        <input 
                          type="text" 
                          required
                          className="glass-input" 
                          placeholder="EMP001" 
                          value={regEmployeeId}
                          onChange={(e) => setRegEmployeeId(e.target.value)}
                        />
                      </div>
                      <div>
                        <label className="glass-label">Security Role</label>
                        <select 
                          className="glass-input"
                          value={regRole}
                          onChange={(e) => setRegRole(e.target.value)}
                        >
                          <option value="EMPLOYEE">Employee</option>
                          <option value="MANAGER">Manager</option>
                          <option value="HR">HR Specialist</option>
                          <option value="ADMIN">System Admin</option>
                        </select>
                      </div>
                    </>
                  )}

                  <button type="submit" className="glass-btn glass-btn-primary w-full py-2.5 rounded-xl font-medium mt-1.5 interactive-item text-xs">
                    {isRegistering ? 'Register Access' : 'Authenticate Access'}
                  </button>

                  <div className="text-center mt-1">
                    <button 
                      type="button" 
                      onClick={() => {
                        setIsRegistering(!isRegistering);
                        setAuthError('');
                        setAuthSuccess('');
                      }}
                      className="text-xs text-white/60 hover:text-white underline transition-colors"
                    >
                      {isRegistering ? 'Already have an account? Sign In' : "Don't have an account? Sign Up"}
                    </button>
                  </div>
                </form>
              </div>
            </div>
          )}

          {/* Footer quote */}
          <div className="mt-auto py-6 border-t border-white/5 text-center">
            <span className="text-[10px] tracking-widest uppercase text-white/40 block mb-1">Visionary Workforce</span>
            <p className="text-xs text-white/70 italic font-serif">
              "We imagined an <em className="text-white">organization with no ending</em>."
            </p>
            <span className="text-[10px] tracking-widest text-white/50 uppercase font-medium mt-1 block">Marcus Aurelio</span>
          </div>

        </div>
      ) : (
        // --- LIVE APP WITH PANELS (WHEN AUTHENTICATED) ---
        <div className="relative z-10 flex flex-col lg:flex-row min-h-screen w-full p-4 lg:p-6 gap-6">
          
          {/* LEFT PANEL */}
          <div className="relative w-full lg:w-[52%] flex flex-col min-h-[calc(100vh-2rem)] lg:min-h-[calc(100vh-3rem)]">
            <div className="absolute inset-0 liquid-glass-strong rounded-[2rem] z-0" />
            
            <div className="relative z-10 flex flex-col flex-1 p-6 lg:p-8">
              {/* Header / Nav */}
              <div className="flex items-center justify-between mb-8">
                <div className="flex items-center gap-2">
                  <div className="w-8 h-8 rounded-full bg-white/10 flex items-center justify-center liquid-glass">
                    <Sparkles className="w-4 h-4 text-white" />
                  </div>
                  <span className="font-semibold text-xl tracking-tighter text-white">bloom <em className="text-white/60">ems</em></span>
                </div>
                
                <div className="flex items-center gap-2">
                  {/* Notification Bell */}
                  <div className="relative">
                    <button 
                      onClick={() => setShowNotificationsDropdown(!showNotificationsDropdown)}
                      className="w-10 h-10 rounded-full bg-white/5 hover:bg-white/10 flex items-center justify-center text-white/80 hover:text-white liquid-glass interactive-item relative"
                    >
                      <Bell className="w-4 h-4" />
                      {notifications.filter(n => !n.read).length > 0 && (
                        <span className="absolute -top-1 -right-1 w-5 h-5 bg-white text-black text-[9px] font-bold rounded-full flex items-center justify-center border border-black">
                          {notifications.filter(n => !n.read).length}
                        </span>
                      )}
                    </button>
                    
                    {showNotificationsDropdown && (
                      <div 
                        className="absolute right-0 mt-2 liquid-glass-strong rounded-2xl p-4 shadow-2xl z-50 text-left border border-white/10" 
                        style={{ width: '280px', right: 0 }}
                      >
                        <div className="flex justify-between items-center border-b border-white/10 pb-2 mb-3">
                          <span className="text-xs font-semibold text-white/80">Notifications</span>
                          {notifications.filter(n => !n.read).length > 0 && (
                            <button 
                              onClick={handleMarkAllNotificationsRead}
                              className="text-[10px] text-white/50 hover:text-white underline"
                            >
                              Mark all read
                            </button>
                          )}
                        </div>
                        
                        <div className="max-h-60 overflow-y-auto custom-scroll pr-1 flex flex-col gap-2">
                          {notifications.map(n => (
                            <div 
                              key={n.id} 
                              className={`p-2.5 rounded-xl text-xs flex justify-between items-start gap-2 ${n.read ? 'bg-white/5 opacity-60' : 'bg-white/10'}`}
                            >
                              <div className="flex-1">
                                <p className="text-white leading-normal">{n.message}</p>
                                <span className="text-[9px] text-white/40 mt-1 block">
                                  {new Date(n.createdAt).toLocaleString()}
                                </span>
                              </div>
                              {!n.read && (
                                <button 
                                  onClick={() => handleMarkNotificationRead(n.id)}
                                  className="text-[10px] text-white/40 hover:text-white shrink-0 mt-0.5"
                                >
                                  Mark read
                                </button>
                              )}
                            </div>
                          ))}
                          {notifications.length === 0 && (
                            <div className="text-xs text-white/40 py-6 text-center">No notifications yet.</div>
                          )}
                        </div>
                      </div>
                    )}
                  </div>

                  {/* Mobile Logout */}
                  <button 
                    onClick={handleLogout}
                    className="lg:hidden w-10 h-10 rounded-full bg-white/5 hover:bg-white/10 flex items-center justify-center text-white/80 hover:text-white liquid-glass interactive-item"
                  >
                    <LogOut className="w-4 h-4" />
                  </button>
                </div>
              </div>

              {/* Central Content Space */}
              <div className="flex-1 flex flex-col justify-center">
                {/* VIEW: DASHBOARD */}
                {activeTab === 'dashboard' && (
                  <div className="flex flex-col gap-4">
                    <div className="grid grid-cols-2 gap-4">
                      <div className="liquid-glass p-4 rounded-2xl text-left">
                        <span className="glass-label">Total Employees</span>
                        <span className="text-3xl font-semibold tracking-tight">{dashboard?.totalEmployees ?? '—'}</span>
                      </div>
                      <div className="liquid-glass p-4 rounded-2xl text-left">
                        <span className="glass-label">Active / Leave</span>
                        <span className="text-3xl font-semibold tracking-tight">
                          {dashboard?.activeEmployees ?? '—'} <span className="text-lg text-white/50 font-normal">/ {dashboard?.employeesOnLeave ?? '—'}</span>
                        </span>
                      </div>
                    </div>

                    <div className="liquid-glass p-5 rounded-2xl text-left">
                      <h3 className="text-sm font-semibold tracking-wider uppercase text-white/70 mb-3">Leave Breakdown</h3>
                      <div className="flex flex-col gap-2">
                        {dashboard?.leaveStatistics && Object.entries(dashboard.leaveStatistics).length > 0 ? (
                          Object.entries(dashboard.leaveStatistics).map(([status, count]) => (
                            <div key={status} className="flex justify-between items-center text-sm">
                              <span className="capitalize text-white/60">{status.toLowerCase()}</span>
                              <span className="font-semibold">{count}</span>
                            </div>
                          ))
                        ) : (
                          <div className="text-xs text-white/40">No leave requests logged.</div>
                        )}
                      </div>
                    </div>

                    <div className="liquid-glass p-5 rounded-2xl text-left">
                      <h3 className="text-sm font-semibold tracking-wider uppercase text-white/70 mb-3">Avg Performance by Department</h3>
                      <div className="flex flex-col gap-2">
                        {dashboard?.performanceStatistics && Object.entries(dashboard.performanceStatistics).length > 0 ? (
                          Object.entries(dashboard.performanceStatistics).map(([dept, rating]) => (
                            <div key={dept} className="flex justify-between items-center text-sm">
                              <span className="text-white/60">{dept}</span>
                              <span className="font-semibold">{Number(rating).toFixed(1)} ★</span>
                            </div>
                          ))
                        ) : (
                          <div className="text-xs text-white/40">No review ratings computed yet.</div>
                        )}
                      </div>
                    </div>
                  </div>
                )}

                {/* VIEW: EMPLOYEE DIRECTORY */}
                {activeTab === 'employees' && (
                  <div className="flex flex-col gap-4">
                    <div className="flex justify-between items-center gap-2">
                      <h3 className="text-base font-semibold text-white/80">Employees ({employees.length})</h3>
                      <button 
                        onClick={openAddEmployeeModal}
                        className="glass-btn glass-btn-primary px-3 py-1.5 rounded-lg text-xs font-semibold interactive-item flex items-center gap-1"
                      >
                        <Plus className="w-3.5 h-3.5" /> Add New
                      </button>
                    </div>

                    {/* Search controls */}
                    <div className="liquid-glass p-4 rounded-xl flex flex-col gap-2">
                      <div className="grid grid-cols-2 gap-2">
                        <input 
                          type="text" 
                          placeholder="Search by name..." 
                          className="glass-input text-xs" 
                          value={employeeSearch.name}
                          onChange={(e) => setEmployeeSearch({...employeeSearch, name: e.target.value})}
                        />
                        <input 
                          type="text" 
                          placeholder="Search by skill..." 
                          className="glass-input text-xs" 
                          value={employeeSearch.skill}
                          onChange={(e) => setEmployeeSearch({...employeeSearch, skill: e.target.value})}
                        />
                      </div>
                      <button 
                        onClick={() => {
                          setEmployeePage(0);
                          fetchEmployees();
                        }}
                        className="glass-btn w-full py-1.5 rounded-lg text-xs font-semibold interactive-item flex items-center justify-center gap-1.5"
                      >
                        <Search className="w-3.5 h-3.5" /> Filter Results
                      </button>
                    </div>

                    <div className="flex flex-col gap-2">
                      {employees.map(emp => (
                        <div key={emp.employeeId} className="liquid-glass p-4 rounded-xl flex items-center justify-between gap-4">
                          <div className="text-left flex-1 min-w-0">
                            <div className="font-semibold text-sm truncate text-white">
                              {emp.firstName} {emp.lastName}
                              <span className="text-xs font-normal text-white/50 ml-2">({emp.employeeId})</span>
                            </div>
                            <div className="text-xs text-white/60 truncate mt-0.5">{emp.designation || 'Specialist'} • {emp.departmentId || 'General'}</div>
                            <div className="flex flex-wrap gap-1 mt-2">
                              {emp.skills && emp.skills.map(s => (
                                <span key={s} className="text-[10px] bg-white/10 text-white/80 px-1.5 py-0.5 rounded-full">{s}</span>
                              ))}
                            </div>
                          </div>
                          <div className="flex gap-2 shrink-0">
                            <button 
                              onClick={() => openEditEmployeeModal(emp)}
                              className="w-8 h-8 rounded-full bg-white/10 flex items-center justify-center interactive-item"
                            >
                              <Edit className="w-3.5 h-3.5 text-white/80" />
                            </button>
                            <button 
                              onClick={() => handleDeleteEmployee(emp.employeeId)}
                              className="w-8 h-8 rounded-full bg-white/10 flex items-center justify-center interactive-item"
                            >
                              <Trash2 className="w-3.5 h-3.5 text-white/80" />
                            </button>
                          </div>
                        </div>
                      ))}
                      {employees.length === 0 && (
                        <div className="text-xs text-white/40 py-6 text-center">No employee records match search filter.</div>
                      )}
                    </div>

                    {/* Pagination controls */}
                    {employeeTotalPages > 1 && (
                      <div className="flex justify-between items-center mt-2 px-2">
                        <button 
                          disabled={employeePage === 0}
                          onClick={() => setEmployeePage(prev => Math.max(0, prev - 1))}
                          className="w-8 h-8 rounded-full bg-white/10 flex items-center justify-center disabled:opacity-30 interactive-item"
                        >
                          <ChevronLeft className="w-4 h-4 text-white" />
                        </button>
                        <span className="text-xs text-white/60">Page {employeePage + 1} of {employeeTotalPages}</span>
                        <button 
                          disabled={employeePage >= employeeTotalPages - 1}
                          onClick={() => setEmployeePage(prev => prev + 1)}
                          className="w-8 h-8 rounded-full bg-white/10 flex items-center justify-center disabled:opacity-30 interactive-item"
                        >
                          <ChevronRight className="w-4 h-4 text-white" />
                        </button>
                      </div>
                    )}
                  </div>
                )}

                {/* VIEW: LEAVE PORTAL */}
                {activeTab === 'leaves' && (
                  <div className="flex flex-col gap-4">
                    <div className={isManagerOrAdmin ? "grid grid-cols-1 md:grid-cols-2 gap-4" : "max-w-md mx-auto w-full"}>
                      {/* Apply Leave Form */}
                      <div className="liquid-glass p-5 rounded-2xl text-left">
                        <h3 className="text-sm font-semibold tracking-wider uppercase text-white/70 mb-4">Request Leave</h3>
                        <form onSubmit={handleApplyLeave} className="flex flex-col gap-3">
                          {leaveError && <div className="text-xs text-white bg-black/50 border border-white/20 p-2 rounded-lg text-center">{leaveError}</div>}
                          {leaveSuccess && <div className="text-xs text-white bg-white/10 border border-white/20 p-2 rounded-lg text-center">{leaveSuccess}</div>}
                          
                          <div>
                            <label className="glass-label">Employee ID</label>
                            <input 
                              type="text" 
                              required
                              placeholder="EMP001" 
                              className={`glass-input text-xs ${!isStaff ? 'opacity-70 cursor-not-allowed' : ''}`}
                              value={leaveEmpId}
                              onChange={(e) => setLeaveEmpId(e.target.value)}
                              readOnly={!isStaff}
                            />
                          </div>

                          <div className="grid grid-cols-2 gap-2">
                            <div>
                              <label className="glass-label">Start Date</label>
                              <input 
                                type="date" 
                                required
                                className="glass-input text-xs" 
                                value={leaveStartDate}
                                onChange={(e) => setLeaveStartDate(e.target.value)}
                              />
                            </div>
                            <div>
                              <label className="glass-label">End Date</label>
                              <input 
                                type="date" 
                                required
                                className="glass-input text-xs" 
                                value={leaveEndDate}
                                onChange={(e) => setLeaveEndDate(e.target.value)}
                              />
                            </div>
                          </div>

                          <div>
                            <label className="glass-label">Reason</label>
                            <input 
                              type="text" 
                              required
                              placeholder="Vacation / Personal / Medical" 
                              className="glass-input text-xs" 
                              value={leaveReason}
                              onChange={(e) => setLeaveReason(e.target.value)}
                            />
                          </div>

                          <button type="submit" className="glass-btn glass-btn-primary w-full py-2.5 rounded-lg text-xs font-semibold mt-1 interactive-item">
                            Submit Request
                          </button>
                        </form>
                      </div>

                      {/* Leaves list for Approval */}
                      {isManagerOrAdmin && (
                        <div className="liquid-glass p-5 rounded-2xl text-left flex flex-col min-h-[300px]">
                          <h3 className="text-sm font-semibold tracking-wider uppercase text-white/70 mb-4">Pending Requests</h3>
                          <div className="flex-1 overflow-y-auto custom-scroll pr-1 flex flex-col gap-2">
                            {leaves.filter(l => l.status === 'PENDING').map(l => (
                              <div key={l.id} className="liquid-glass p-3 rounded-xl flex flex-col gap-2">
                                <div className="flex justify-between items-center text-xs">
                                  <span className="font-semibold text-white">{l.employeeId}</span>
                                  <span className="text-white/50">{l.startDate} to {l.endDate}</span>
                                </div>
                                <div className="text-xs text-white/60 italic font-serif">"{l.reason}"</div>
                                <div className="flex justify-end gap-2 mt-1">
                                  <button 
                                    onClick={() => handleRejectLeave(l.id)}
                                    className="glass-btn bg-black/40 hover:bg-black/60 px-2.5 py-1 rounded text-[10px] interactive-item text-white/80"
                                  >
                                    Reject
                                  </button>
                                  <button 
                                    onClick={() => handleApproveLeave(l.id)}
                                    className="glass-btn glass-btn-primary px-2.5 py-1 rounded text-[10px] interactive-item"
                                  >
                                    Approve
                                  </button>
                                </div>
                              </div>
                            ))}
                            {leaves.filter(l => l.status === 'PENDING').length === 0 && (
                              <div className="text-xs text-white/40 my-auto text-center">No pending leave approvals found.</div>
                            )}
                          </div>
                        </div>
                      )}
                    </div>
                  </div>
                )}

                {/* VIEW: ATTENDANCE */}
                {activeTab === 'attendance' && (
                  <div className="flex flex-col gap-4 max-w-sm mx-auto">
                    <div className="liquid-glass p-6 rounded-2xl text-left">
                      <div className="text-center mb-4">
                        <Clock className="w-8 h-8 text-white/70 mx-auto mb-2" />
                        <h3 className="text-sm font-semibold tracking-wider uppercase text-white/70">Attendance Check-in</h3>
                        <p className="text-xs text-white/40 mt-1">Clock check-in and check-out logs</p>
                      </div>

                      <form className="flex flex-col gap-3">
                        {attError && <div className="text-xs text-white bg-black/50 border border-white/20 p-2.5 rounded-lg text-center">{attError}</div>}
                        {attMessage && <div className="text-xs text-white bg-white/10 border border-white/20 p-2.5 rounded-lg text-center">{attMessage}</div>}
                        
                        <div>
                          <label className="glass-label">Employee ID</label>
                          <input 
                            type="text" 
                            required
                            placeholder="EMP001" 
                            className={`glass-input ${!isStaff ? 'opacity-70 cursor-not-allowed' : ''}`}
                            value={attEmpId}
                            onChange={(e) => setAttEmpId(e.target.value)}
                            readOnly={!isStaff}
                          />
                        </div>

                        <div className="grid grid-cols-2 gap-2 mt-2">
                          <button 
                            type="button"
                            onClick={handleCheckIn}
                            className="glass-btn glass-btn-primary py-2.5 rounded-lg font-semibold interactive-item"
                          >
                            Check In
                          </button>
                          <button 
                            type="button"
                            onClick={handleCheckOut}
                            className="glass-btn py-2.5 rounded-lg font-semibold interactive-item"
                          >
                            Check Out
                          </button>
                        </div>
                      </form>
                    </div>
                  </div>
                )}

              </div>

              {/* Navigation Pills */}
              <div className="flex flex-wrap justify-center gap-2 mt-auto pt-2">
                <button 
                  onClick={() => setActiveTab('dashboard')}
                  className={`glass-btn text-xs px-4 py-2 interactive-item ${activeTab === 'dashboard' ? 'glass-btn-primary' : ''}`}
                >
                  Overview Dashboard
                </button>
                {isStaff && (
                  <button 
                    onClick={() => setActiveTab('employees')}
                    className={`glass-btn text-xs px-4 py-2 interactive-item ${activeTab === 'employees' ? 'glass-btn-primary' : ''}`}
                  >
                    Employee Manager
                  </button>
                )}
                <button 
                  onClick={() => setActiveTab('leaves')}
                  className={`glass-btn text-xs px-4 py-2 interactive-item ${activeTab === 'leaves' ? 'glass-btn-primary' : ''}`}
                >
                  Leave Requests
                </button>
                <button 
                  onClick={() => setActiveTab('attendance')}
                  className={`glass-btn text-xs px-4 py-2 interactive-item ${activeTab === 'attendance' ? 'glass-btn-primary' : ''}`}
                >
                  Attendance Check
                </button>
              </div>

            </div>

            {/* Bottom Quote */}
            <div className="mt-8 pt-4 border-t border-white/5">
              <span className="text-[10px] tracking-widest uppercase text-white/40 block mb-1">Visionary Workforce</span>
              <p className="text-xs text-white/70 italic font-serif">
                "We imagined an <em className="text-white">organization with no ending</em>."
              </p>
              <div className="flex items-center justify-center gap-4 mt-2">
                <div className="h-px flex-1 bg-white/5" />
                <span className="text-[10px] tracking-widest text-white/50 uppercase font-medium">Marcus Aurelio</span>
                <div className="h-px flex-1 bg-white/5" />
              </div>
            </div>

          </div>

        {/* RIGHT PANEL (48% width, desktop only) */}
        {isAuthenticated && (
          <div className="hidden lg:flex w-[48%] flex-col justify-between min-h-[calc(100vh-3rem)]">
            
            {/* Top Bar socials & account details */}
            <div className="flex justify-between items-center w-full">
              <div className="flex items-center gap-2 bg-white/5 px-4 py-2 rounded-full liquid-glass">
                <a href="https://x.com" target="_blank" rel="noopener noreferrer" className="w-8 h-8 rounded-full bg-white/5 hover:bg-white/10 flex items-center justify-center text-white/80 hover:text-white transition-all interactive-item">
                  <TwitterIcon className="w-4 h-4" />
                </a>
                <a href="https://in.linkedin.com/" target="_blank" rel="noopener noreferrer" className="w-8 h-8 rounded-full bg-white/5 hover:bg-white/10 flex items-center justify-center text-white/80 hover:text-white transition-all interactive-item">
                  <LinkedinIcon className="w-4 h-4" />
                </a>
                <a href="https://www.instagram.com" target="_blank" rel="noopener noreferrer" className="w-8 h-8 rounded-full bg-white/5 hover:bg-white/10 flex items-center justify-center text-white/80 hover:text-white transition-all interactive-item">
                  <InstagramIcon className="w-4 h-4" />
                </a>
                <div className="w-px h-5 bg-white/15 mx-1" />
                <ArrowRight className="w-4 h-4 text-white/40" />
              </div>

              {isAuthenticated && (
                <div className="flex items-center gap-2">
                  <div className="flex items-center gap-1.5 bg-white/5 px-4 py-2 rounded-full liquid-glass text-xs text-white/80 font-medium">
                    <Sparkles className="w-3.5 h-3.5 text-white/60" />
                    <span className="text-[10px] uppercase font-semibold text-white/50 mr-1">({user?.roles[0]?.replace('ROLE_', '') || 'EMPLOYEE'})</span>
                    <span className="truncate max-w-[120px]">{user?.email}</span>
                  </div>
                  <button 
                    onClick={handleLogout}
                    className="w-10 h-10 rounded-full bg-white/5 hover:bg-white/10 flex items-center justify-center text-white/80 hover:text-white liquid-glass interactive-item"
                  >
                    <LogOut className="w-4 h-4" />
                  </button>
                </div>
              )}
            </div>

            {/* Center Ecosystem Card */}
            <div className="my-auto max-w-sm mx-auto w-full">
              <div className="liquid-glass p-6 rounded-[2rem] text-center">
                <h2 className="text-lg font-semibold tracking-tight text-white mb-1">Enter our ecosystem</h2>
                <p className="text-xs text-white/60 mb-4 leading-relaxed">
                  Connect and manage workforce productivity, calculate audits, analyze payroll parameters, and configure leaves autonomously.
                </p>
                
                <div className="flex items-center justify-between bg-white/5 p-3.5 rounded-xl border border-white/5">
                  <div className="text-left">
                    <span className="text-[10px] uppercase text-white/40 tracking-wider font-semibold block">Attrition Rate</span>
                    <span className="text-lg font-bold tracking-tight text-white mt-0.5">
                      {dashboard?.attritionRate ? `${Number(dashboard.attritionRate).toFixed(1)}%` : '0.0%'}
                    </span>
                  </div>
                  <div className="w-8 h-8 rounded-full bg-white/10 flex items-center justify-center">
                    <Wand2 className="w-4 h-4 text-white/80" />
                  </div>
                </div>
              </div>
            </div>

            {/* Bottom Feature Cards */}
            <div className="flex flex-col gap-4 mt-auto">
              <div className="grid grid-cols-2 gap-4">
                <div className="liquid-glass p-5 rounded-[1.75rem] text-left">
                  <div className="w-8 h-8 rounded-full bg-white/10 flex items-center justify-center mb-3">
                    <Wand2 className="w-4.5 h-4.5 text-white/80" />
                  </div>
                  <h4 className="text-xs font-semibold text-white/50 uppercase tracking-wider">Active Departments</h4>
                  <p className="text-2xl font-bold tracking-tight mt-1">{dashboard?.departments ?? '0'}</p>
                </div>

                <div className="liquid-glass p-5 rounded-[1.75rem] text-left">
                  <div className="w-8 h-8 rounded-full bg-white/10 flex items-center justify-center mb-3">
                    <BookOpen className="w-4.5 h-4.5 text-white/80" />
                  </div>
                  <h4 className="text-xs font-semibold text-white/50 uppercase tracking-wider">Running Projects</h4>
                  <p className="text-2xl font-bold tracking-tight mt-1">{dashboard?.projects ?? '0'}</p>
                </div>
              </div>

              {/* Bottom Card: headcounts details */}
              <div className="liquid-glass p-6 rounded-[2rem] text-left">
                <h3 className="text-sm font-semibold tracking-wider uppercase text-white/70 mb-3">Department headcounts</h3>
                <div className="flex flex-col gap-3">
                  {dashboard?.departmentWiseEmployees && Object.entries(dashboard.departmentWiseEmployees).length > 0 ? (
                    Object.entries(dashboard.departmentWiseEmployees).map(([dept, count]) => {
                      const total = dashboard.totalEmployees || 1;
                      const percent = Math.min(100, Math.max(5, (count / total) * 100));
                      return (
                        <div key={dept} className="flex flex-col gap-1">
                          <div className="flex justify-between items-center text-xs">
                            <span className="text-white/70">{dept}</span>
                            <span className="font-semibold text-white">{count} ({Math.round(percent)}%)</span>
                          </div>
                          <div className="w-full h-1 bg-white/5 rounded-full overflow-hidden">
                            <div 
                              className="h-full bg-white/30 rounded-full transition-all duration-500" 
                              style={{ width: `${percent}%` }}
                            />
                          </div>
                        </div>
                      );
                    })
                  ) : (
                    <div className="text-xs text-white/40">No departments setup. Register employees to generate headcounts.</div>
                  )}
                </div>
              </div>
            </div>

          </div>
        )}

      </div>
    )}

      {/* EMPLOYEE ADD/EDIT MODAL */}
      {showEmployeeModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/70 backdrop-blur-sm">
          <div className="relative w-full max-w-md liquid-glass-strong rounded-3xl p-6 shadow-2xl flex flex-col gap-4">
            <div className="flex justify-between items-center border-b border-white/10 pb-3">
              <h3 className="text-lg font-semibold">{editingEmployee ? 'Edit Employee Record' : 'Create Employee Record'}</h3>
              <button 
                onClick={() => setShowEmployeeModal(false)}
                className="w-8 h-8 rounded-full bg-white/5 flex items-center justify-center interactive-item"
              >
                <X className="w-4 h-4 text-white" />
              </button>
            </div>

            <form onSubmit={handleSaveEmployee} className="overflow-y-auto max-h-[70vh] custom-scroll pr-1 flex flex-col gap-3">
              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="glass-label">Employee ID</label>
                  <input 
                    type="text" 
                    required
                    disabled={!!editingEmployee}
                    className="glass-input" 
                    placeholder="EMP001"
                    value={employeeForm.employeeId}
                    onChange={(e) => setEmployeeForm({...employeeForm, employeeId: e.target.value})}
                  />
                </div>
                <div>
                  <label className="glass-label">Designation</label>
                  <input 
                    type="text" 
                    required
                    className="glass-input" 
                    placeholder="Software Engineer"
                    value={employeeForm.designation}
                    onChange={(e) => setEmployeeForm({...employeeForm, designation: e.target.value})}
                  />
                </div>
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="glass-label">First Name</label>
                  <input 
                    type="text" 
                    required
                    className="glass-input" 
                    placeholder="John"
                    value={employeeForm.firstName}
                    onChange={(e) => setEmployeeForm({...employeeForm, firstName: e.target.value})}
                  />
                </div>
                <div>
                  <label className="glass-label">Last Name</label>
                  <input 
                    type="text" 
                    required
                    className="glass-input" 
                    placeholder="Doe"
                    value={employeeForm.lastName}
                    onChange={(e) => setEmployeeForm({...employeeForm, lastName: e.target.value})}
                  />
                </div>
              </div>

              <div>
                <label className="glass-label">Workplace Email</label>
                <input 
                  type="email" 
                  required
                  className="glass-input" 
                  placeholder="john.doe@company.com"
                  value={employeeForm.email}
                  onChange={(e) => setEmployeeForm({...employeeForm, email: e.target.value})}
                />
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="glass-label">Phone Number</label>
                  <input 
                    type="tel" 
                    className="glass-input" 
                    placeholder="+1 555-1234"
                    value={employeeForm.phone}
                    onChange={(e) => setEmployeeForm({...employeeForm, phone: e.target.value})}
                  />
                </div>
                <div>
                  <label className="glass-label">Salary (USD/yr)</label>
                  <input 
                    type="number" 
                    required
                    className="glass-input" 
                    value={employeeForm.salary}
                    onChange={(e) => setEmployeeForm({...employeeForm, salary: e.target.value})}
                  />
                </div>
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="glass-label">Department ID</label>
                  <input 
                    type="text" 
                    className="glass-input" 
                    placeholder="DEP001"
                    value={employeeForm.departmentId}
                    onChange={(e) => setEmployeeForm({...employeeForm, departmentId: e.target.value})}
                  />
                </div>
                <div>
                  <label className="glass-label">Manager Employee ID</label>
                  <input 
                    type="text" 
                    className="glass-input" 
                    placeholder="EMP002"
                    value={employeeForm.managerId}
                    onChange={(e) => setEmployeeForm({...employeeForm, managerId: e.target.value})}
                  />
                </div>
              </div>

              <div>
                <label className="glass-label">Skills (comma-separated)</label>
                <input 
                  type="text" 
                  className="glass-input" 
                  placeholder="Java, Spring Boot, MongoDB"
                  value={employeeForm.skills}
                  onChange={(e) => setEmployeeForm({...employeeForm, skills: e.target.value})}
                />
              </div>

              <button type="submit" className="glass-btn glass-btn-primary w-full py-3 rounded-xl font-medium mt-2 interactive-item">
                {editingEmployee ? 'Apply Changes' : 'Register Employee'}
              </button>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}

export default App;
