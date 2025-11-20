export type ClassStatus = 'SCHEDULED' | 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED';

export interface GymClass {
  id: string;
  name: string;
  description?: string;
  trainerId: string;
  trainerName?: string;
  capacity: number;
  enrolledCount: number;
  startTime: string;
  endTime: string;
  duration: number; // in minutes
  status: ClassStatus;
  room?: string;
  createdAt: string;
  updatedAt: string;
}

export interface ClassBooking {
  id: string;
  classId: string;
  memberId: string;
  status: 'CONFIRMED' | 'CANCELLED' | 'WAITLISTED';
  bookingDate: string;
  createdAt: string;
  updatedAt: string;
}
