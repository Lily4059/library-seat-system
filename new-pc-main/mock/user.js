export default [
  {
    url: '/api/user/profile',
    method: 'get',
    response: () => ({
      code: 200,
      data: {
        id: 1,
        name: '张三',
        studentNo: '20210001',
        creditScore: 95,
        totalStudyTime: 360,
        avatar: '',
        phone: '138****1234'
      }
    })
  }
]