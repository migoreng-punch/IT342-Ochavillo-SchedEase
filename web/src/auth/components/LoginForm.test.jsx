import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { describe, it, expect, vi } from 'vitest';
import { BrowserRouter } from 'react-router-dom';
import { LoginForm } from './LoginForm'; // Use named import {}
import { useAuth } from '../context/AuthContext'; 

vi.mock('../context/AuthContext');

const mockNavigate = vi.fn();
vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual('react-router-dom');
  return { ...actual, useNavigate: () => mockNavigate };
});

describe('LoginForm Component', () => {
  it('navigates to "/" on successful login', async () => {
    // Arrange
    const mockLogin = vi.fn().mockResolvedValue(true);
    vi.mocked(useAuth).mockReturnValue({ login: mockLogin });

    render(
      <BrowserRouter>
        <LoginForm />
      </BrowserRouter>
    );

    // Act
    fireEvent.change(screen.getByLabelText(/username/i), { target: { value: 'testuser' } });
    fireEvent.change(screen.getByLabelText(/password/i), { target: { value: 'password123' } });
    fireEvent.click(screen.getByRole('button', { name: /sign in/i }));

    // Assert
    await waitFor(() => {
      expect(mockNavigate).toHaveBeenCalledWith("/");
    });
  });

  it('displays a server error message on failed login', async () => {
    // Arrange: Simulate the catch block triggering
    const mockLogin = vi.fn().mockRejectedValue(new Error('API Error'));
    vi.mocked(useAuth).mockReturnValue({ login: mockLogin });

    render(
      <BrowserRouter>
        <LoginForm />
      </BrowserRouter>
    );

    // Fill form so it passes Zod validation and hits the login() try/catch
    fireEvent.change(screen.getByLabelText(/username/i), { target: { value: 'wronguser' } });
    fireEvent.change(screen.getByLabelText(/password/i), { target: { value: 'wrongpass' } });
    fireEvent.click(screen.getByRole('button', { name: /sign in/i }));

    // Assert: Match the specific text from your LoginForm's catch block
    await waitFor(() => {
      expect(screen.getByText(/invalid username or password/i)).toBeInTheDocument();
    });
  });
});