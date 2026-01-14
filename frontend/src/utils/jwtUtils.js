/**
 * Utility functions for JWT token decoding and parsing
 */

/**
 * Decode a JWT token without verification (client-side only)
 * @param {string} token - JWT token string
 * @returns {object|null} Decoded token payload or null if invalid
 */
export function decodeJWT(token) {
    try {
        if (!token) return null;
        
        const parts = token.split('.');
        if (parts.length !== 3) return null;
        
        // Decode base64url encoded payload
        const payload = parts[1];
        const decoded = atob(payload.replace(/-/g, '+').replace(/_/g, '/'));
        return JSON.parse(decoded);
    } catch (error) {
        console.error('Error decoding JWT:', error);
        return null;
    }
}

/**
 * Extract scopes from JWT token
 * @param {string} token - JWT access token
 * @returns {string[]} Array of scopes
 */
export function getScopesFromToken(token) {
    const decoded = decodeJWT(token);
    if (!decoded) return [];
    
    // Auth0 typically puts scopes in 'scope' claim as space-separated string
    const scopeString = decoded.scope || decoded.permissions || '';
    if (typeof scopeString === 'string') {
        return scopeString.split(' ').filter(s => s.length > 0);
    }
    if (Array.isArray(scopeString)) {
        return scopeString;
    }
    return [];
}

/**
 * Check if user has admin scope
 * @param {string} token - JWT access token
 * @returns {boolean} True if user has admin:flights scope
 */
export function hasAdminScope(token) {
    const scopes = getScopesFromToken(token);
    return scopes.includes('admin:flights') || scopes.includes('SCOPE_admin:flights');
}

/**
 * Extract roles from JWT token
 * Auth0 roles can be in different places:
 * - ID token: usually in namespace like 'https://your-domain/roles'
 * - Access token: might be in 'roles' claim or namespace
 * @param {string} token - JWT token (access or ID token)
 * @param {string} auth0Domain - Optional Auth0 domain for namespace lookup
 * @returns {string[]} Array of roles
 */
export function getRolesFromToken(token, auth0Domain = null) {
    const decoded = decodeJWT(token);
    if (!decoded) return [];
    
    let roles = [];
    
    // 1. Check Auth0 namespace format: https://your-domain/roles
    if (auth0Domain) {
        const namespace = `https://${auth0Domain}/roles`;
        if (decoded[namespace]) {
            roles = decoded[namespace];
        }
    }
    
    // 2. Check common namespace patterns
    if (roles.length === 0) {
        const possibleNamespaces = [
            'https://api.airline.com/roles',
            'https://airline.com/roles',
            decoded.iss ? `https://${decoded.iss.replace('https://', '').split('/')[0]}/roles` : null
        ].filter(Boolean);
        
        for (const ns of possibleNamespaces) {
            if (decoded[ns]) {
                roles = decoded[ns];
                break;
            }
        }
    }
    
    // 3. Check standard 'roles' claim
    if (roles.length === 0 && decoded.roles) {
        roles = decoded.roles;
    }
    
    // 4. Check other common role claim names
    if (roles.length === 0) {
        roles = decoded['http://schemas.microsoft.com/ws/2008/06/identity/claims/role'] || [];
    }
    
    // Normalize to array
    if (typeof roles === 'string') {
        return [roles];
    }
    if (Array.isArray(roles)) {
        return roles;
    }
    return [];
}

/**
 * Check if user has admin role
 * @param {string} token - JWT token (access or ID token)
 * @param {string} auth0Domain - Optional Auth0 domain for namespace lookup
 * @returns {boolean} True if user has admin role
 */
export function hasAdminRole(token, auth0Domain = null) {
    const roles = getRolesFromToken(token, auth0Domain);
    return roles.some(r => {
        const roleLower = r.toLowerCase();
        return roleLower === 'admin' || 
               roleLower === 'role_admin' || 
               roleLower.includes('admin');
    });
}

/**
 * Check if user is admin (has admin scope or role)
 * @param {string} token - JWT access token
 * @param {string} auth0Domain - Optional Auth0 domain for namespace lookup
 * @returns {boolean} True if user is admin
 */
export function isAdmin(token, auth0Domain = null) {
    if (!token) return false;
    return hasAdminScope(token) || hasAdminRole(token, auth0Domain);
}
