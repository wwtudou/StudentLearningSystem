import api from './index'

export const fetchDictTypes = () => api.get('/dicts/types')
export const createDictType = (data) => api.post('/dicts/types', data)
export const updateDictType = (code, data) => api.put(`/dicts/types/${code}`, data)
export const deleteDictType = (code) => api.delete(`/dicts/types/${code}`)
export const fetchDictItems = (typeCode) => api.get(`/dicts/types/${typeCode}/items`)
export const createDictItem = (typeCode, data) => api.post(`/dicts/types/${typeCode}/items`, data)
export const updateDictItem = (typeCode, itemCode, data) => api.put(`/dicts/types/${typeCode}/items/${itemCode}`, data)
export const deleteDictItem = (typeCode, itemCode) => api.delete(`/dicts/types/${typeCode}/items/${itemCode}`)
