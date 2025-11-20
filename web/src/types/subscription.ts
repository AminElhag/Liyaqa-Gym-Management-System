export type SubscriptionStatus = 'ACTIVE' | 'EXPIRED' | 'PENDING' | 'CANCELLED';

export interface SubscriptionPlan {
  id: string;
  name: string;
  description?: string;
  price: number;
  duration: number; // in days
  features: string[];
  isActive: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface Subscription {
  id: string;
  memberId: string;
  planId: string;
  plan?: SubscriptionPlan;
  status: SubscriptionStatus;
  startDate: string;
  endDate: string;
  autoRenew: boolean;
  createdAt: string;
  updatedAt: string;
}
