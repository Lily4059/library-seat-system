export default [
  {
    url: '/api/seats',
    method: 'get',
    response: ({ query }) => {
      const floor = query.floor || '1F'
      // 模拟不同楼层的座位数据
      const seatsData = {
        '1F': [
          { id: 1, seatNo: 'A01', status: 'occupied', type: 'silent', x: 0, y: 0 },
          { id: 2, seatNo: 'A02', status: 'occupied', type: 'power', x: 1, y: 0 },
          { id: 3, seatNo: 'A03', status: 'temp_leave', type: 'window', x: 2, y: 0 },
          { id: 4, seatNo: 'A04', status: 'available', type: 'discussion', x: 3, y: 0 },
          { id: 5, seatNo: 'B01', status: 'occupied', type: 'silent', x: 0, y: 1 },
          { id: 6, seatNo: 'B02', status: 'available', type: 'power', x: 1, y: 1 },
          { id: 7, seatNo: 'B03', status: 'occupied', type: 'window', x: 2, y: 1 },
          { id: 8, seatNo: 'B04', status: 'available', type: 'discussion', x: 3, y: 1 },
        ],
        '2F': [
          { id: 9, seatNo: 'C01', status: 'available', type: 'silent', x: 0, y: 0 },
          { id: 10, seatNo: 'C02', status: 'occupied', type: 'power', x: 1, y: 0 },
          { id: 11, seatNo: 'C03', status: 'temp_leave', type: 'window', x: 2, y: 0 },
          { id: 12, seatNo: 'C04', status: 'available', type: 'discussion', x: 3, y: 0 },
        ]
      }
      return {
        code: 200,
        data: seatsData[floor] || []
      }
    }
  }
]
