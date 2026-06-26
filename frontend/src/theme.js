// Ant Design theme — modern "espresso" palette, soft rounded, subtle shadows.
const FONT = "'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif"

export const theme = {
  token: {
    colorPrimary: '#7C3AED',
    colorInfo: '#7C3AED',
    colorLink: '#7C3AED',
    colorSuccess: '#16a34a',
    colorWarning: '#e0a008',
    colorError: '#dc2626',
    borderRadius: 10,
    controlHeight: 38,
    fontFamily: FONT,
    fontSize: 14,
    colorBgLayout: '#f5f5fb',
    colorTextHeading: '#1f1b2e',
    boxShadowSecondary: '0 6px 20px rgba(17,24,39,.06)',
  },
  components: {
    Layout: {
      siderBg: '#1b1830',
      headerBg: 'rgba(255,255,255,.8)',
      bodyBg: '#f4f5f7',
      headerHeight: 60,
    },
    Menu: {
      darkItemBg: 'transparent',
      darkSubMenuItemBg: 'transparent',
      darkItemSelectedBg: '#7C3AED',
      darkItemHoverBg: 'rgba(255,255,255,.07)',
      darkItemColor: '#cfc7bd',
      darkItemSelectedColor: '#ffffff',
      itemBorderRadius: 10,
      itemMarginInline: 8,
      itemHeight: 42,
    },
    Card: { borderRadiusLG: 16, paddingLG: 20 },
    Button: { borderRadius: 10, controlHeight: 38, primaryShadow: '0 6px 16px rgba(124,58,237,.25)', fontWeight: 500 },
    Table: { headerBg: '#f7f6fb', headerColor: '#6b6480', borderColor: '#f1f0f7', rowHoverBg: '#faf8ff', cellPaddingBlock: 12 },
    Input: { borderRadius: 10, controlHeight: 38 },
    InputNumber: { borderRadius: 10, controlHeight: 38 },
    Select: { borderRadius: 10, controlHeight: 38 },
    DatePicker: { borderRadius: 10, controlHeight: 38 },
    Modal: { borderRadiusLG: 18 },
    Tag: { borderRadiusSM: 6 },
    Segmented: { borderRadius: 10 },
  },
}
