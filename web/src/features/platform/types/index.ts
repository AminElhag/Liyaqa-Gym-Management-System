export interface PlatformAdmin {
  id: string;
  email: string;
  name: string;
  role: 'SUPER_ADMIN' | 'PLATFORM_ADMIN';
  isActive: boolean;
  createdAt: string;
}

export interface PlatformAuthState {
  admin: PlatformAdmin | null;
  token: string | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  error: string | null;
}

export interface PlatformMetrics {
  totalTenants: number;
  activeSubscriptions: number;
  monthlyRevenue: number;
  totalMembers: number;
  tenantGrowth: number;
  subscriptionGrowth: number;
  revenueGrowth: number;
  memberGrowth: number;
}

export interface PlatformMetricsState {
  metrics: PlatformMetrics | null;
  loading: boolean;
  error: string | null;
}

export interface Tenant {
  id: string;
  name: string;
  nameArabic?: string;
  slug: string;
  businessType: 'GYM' | 'FITNESS_CENTER' | 'SPORTS_CLUB';
  status: 'ACTIVE' | 'TRIAL' | 'SUSPENDED' | 'CANCELLED';
  subscriptionPlan: 'STARTER' | 'PROFESSIONAL' | 'ENTERPRISE';
  billingCycle: 'MONTHLY' | 'QUARTERLY' | 'ANNUAL';
  totalMembers: number;
  maxMembers: number;
  createdAt: string;
  subscriptionStartDate: string;
  subscriptionEndDate: string;
  trialEndsAt?: string;
  ownerId: string;
  ownerName: string;
  ownerEmail: string;
  ownerPhone: string;
  contactEmail: string;
  contactPhone: string;
  city: string;
  country: string;
  vatNumber?: string;
  commercialRegistration?: string;
}

export interface TenantsState {
  tenants: Tenant[];
  currentTenant: Tenant | null;
  loading: boolean;
  error: string | null;
  pagination: {
    page: number;
    limit: number;
    total: number;
    totalPages: number;
  };
  filters: {
    status: string;
    plan: string;
    search: string;
  };
}

export interface CreateTenantRequest {
  name: string;
  nameArabic?: string;
  slug: string;
  businessType: 'GYM' | 'FITNESS_CENTER' | 'SPORTS_CLUB';
  plan: 'STARTER' | 'PROFESSIONAL' | 'ENTERPRISE';
  billingCycle: 'MONTHLY' | 'QUARTERLY' | 'ANNUAL';
  startWithTrial: boolean;
  ownerName: string;
  ownerEmail: string;
  ownerPhone: string;
  contactEmail: string;
  contactPhone: string;
  city: string;
  country: string;
  vatNumber?: string;
  commercialRegistration?: string;
}

export interface UpdateTenantRequest {
  name?: string;
  nameArabic?: string;
  businessType?: 'GYM' | 'FITNESS_CENTER' | 'SPORTS_CLUB';
  contactEmail?: string;
  contactPhone?: string;
  city?: string;
  country?: string;
  vatNumber?: string;
  commercialRegistration?: string;
}
