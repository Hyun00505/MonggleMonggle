const colorMap = {
  회색: '#CBD5E0', // 소프트 그레이
  갈색: '#B98A5B', // 웜 소프트 브라운
  주황색: '#F6AD55', // 파스텔 오렌지
  노란색: '#F6E05E', // 라이트 옐로
  초록색: '#9AE6B4', // 파스텔 민트그린
  파란색: '#90CDF4', // 스카이 블루
  보라색: '#B794F4', // 라벤더 퍼플
  분홍색: '#FBB6CE', // 소프트 핑크
  빨간색: '#F56565', // 살몬 레드
};

export function getColorHex(colorName) {
  return colorMap[colorName] || '#FFFFFF';
}

