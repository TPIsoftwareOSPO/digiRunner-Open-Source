/**
 * SMART Client 前端常數定義。
 *
 * 結構對應後端 enum：SmartClientType、SmartLaunchMode。
 * 規格依據：HL7 SMART App Launch STU2.2。
 * 值是規範定義的固定集合，不會隨系統變動。
 */

// ==================== Client 認證類型 ====================

export interface SmartClientTypeOption {
  /** 對應後端 SmartClientType.key() */
  key: string;
  /** 該類型允許的 tokenEndpointAuthMethod，空陣列 = 不應有 */
  allowedAuthMethods: string[];
  /** 是否需要 jwksUri 或 jwks */
  requiresJwk: boolean;
}

/**
 * SMART Client 認證類型清單。
 *
 * 對應後端 SmartClientType enum，攜帶交叉驗證規則。
 * 前端用此常數建立 dropdown options 和動態表單行為
 * （例如選了 public → authMethod 隱藏、JWK 欄位隱藏）。
 */
export const SMART_CLIENT_TYPES: SmartClientTypeOption[] = [
  { key: 'public', allowedAuthMethods: [], requiresJwk: false },
  { key: 'confidential-symmetric', allowedAuthMethods: ['client_secret_basic', 'client_secret_post'], requiresJwk: false },
  { key: 'confidential-asymmetric', allowedAuthMethods: ['private_key_jwt'], requiresJwk: true },
];

// ==================== 啟動模式 ====================

/**
 * SMART App Launch 啟動模式清單。
 *
 * 對應後端 SmartLaunchMode enum。
 */
export const SMART_LAUNCH_MODES = ['standalone', 'ehr', 'both'] as const;

export type SmartLaunchMode = typeof SMART_LAUNCH_MODES[number];

// ==================== 自動核准 ====================

export const SMART_AUTO_APPROVE_OPTIONS = ['Y', 'N'] as const;

export type SmartAutoApprove = typeof SMART_AUTO_APPROVE_OPTIONS[number];

// ==================== 身份驗證方式（IdP Type） ====================

/**
 * Gateway 支援的 IdP 類型清單。
 * 對應後端 DgrIdpInfoService 的合法值。
 */
export const SMART_IDP_TYPES = ['GOOGLE', 'MS', 'OIDC', 'LDAP', 'JDBC', 'API', 'CUS'] as const;

export type SmartIdpType = typeof SMART_IDP_TYPES[number];

// ==================== 預定義 Scope 選項 ====================

export interface SmartScopeOption {
  scope: string;
  category: 'identity' | 'context' | 'permission' | 'other';
}

/**
 * SMART on FHIR 預定義 scope 選項。
 * 分類對應 SMART App Launch STU2.2 的 scope 語意。
 */
export const SMART_SCOPE_OPTIONS: SmartScopeOption[] = [
  { scope: 'openid', category: 'identity' },
  { scope: 'fhirUser', category: 'identity' },
  { scope: 'profile', category: 'identity' },
  { scope: 'launch', category: 'context' },
  { scope: 'launch/patient', category: 'context' },
  { scope: 'launch/encounter', category: 'context' },
  { scope: 'patient/*.cruds', category: 'permission' },
  { scope: 'user/*.cruds', category: 'permission' },
  { scope: 'system/*.cruds', category: 'permission' },
  { scope: 'offline_access', category: 'other' },
];

export const SMART_SCOPE_CATEGORIES = [
  { key: 'identity', label: 'smart_client.scope_cat.identity' },
  { key: 'context', label: 'smart_client.scope_cat.context' },
  { key: 'permission', label: 'smart_client.scope_cat.permission' },
  { key: 'other', label: 'smart_client.scope_cat.other' },
] as const;
