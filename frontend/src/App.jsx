import { LoginController } from './auth/LoginController';
import WelcomePage from './pages/WelcomePage';

export default function App() {
  return (
    <LoginController>
      <WelcomePage />
    </LoginController>
  );
}
