// Ant Design theme — modern "espresso" palette, soft rounded, subtle shadows.
const FONT = "'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif"

export const theme = {
  token: {
    colorPrimary: '#a0522d',
    colorInfo: '#a0522d',
    colorLink: '#a0522d',
    colorSuccess: '#16a34a',
    colorWarning: '#e0a008',
    colorError: '#dc2626',
    borderRadius: 10,
    controlHeight: 38,
    fontFamily: FONT,
    fontSize: 14,
    colorBgLayout: '#f4f5f7',
    colorTextHeading: '#1f1b18',
    boxShadowSecondary: '0 6px 20px rgba(17,24,39,.06)',
  },
  components: {
    Layout: {
      siderBg: '#1c1714',
      headerBg: 'rgba(255,255,255,.8)',
      bodyBg: '#f4f5f7',
      headerHeight: 60,
    },
    Menu: {
      darkItemBg: 'transparent',
      darkSubMenuItemBg: 'transparent',
      darkItemSelectedBg: '#a0522d',
      darkItemHoverBg: 'rgba(255,255,255,.07)',
      darkItemColor: '#cfc7bd',
      darkItemSelectedColor: '#ffffff',
      itemBorderRadius: 10,
      itemMarginInline: 8,
      itemHeight: 42,
    },
    Card: { borderRadiusLG: 16, paddingLG: 20 },
    Button: { borderRadius: 10, controlHeight: 38, primaryShadow: '0 6px 16px rgba(160,82,45,.22)', fontWeight: 500 },
    Table: { headerBg: '#faf9f7', headerColor: '#6b5d52', borderColor: '#f0efec', rowHoverBg: '#faf7f4', cellPaddingBlock: 12 },
    Input: { borderRadius: 10, controlHeight: 38 },
    InputNumber: { borderRadius: 10, controlHeight: 38 },
    Select: { borderRadius: 10, controlHeight: 38 },
    DatePicker: { borderRadius: 10, controlHeight: 38 },
    Modal: { borderRadiusLG: 18 },
    Tag: { borderRadiusSM: 6 },
    Segmented: { borderRadius: 10 },
  },
}
