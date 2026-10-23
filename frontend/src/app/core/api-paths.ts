import { environment } from '../../environments/environment';

/** Central API base path; environment-specific host routing is handled by Nginx in Compose. */
export const API_BASE_URL = environment.apiBaseUrl;
