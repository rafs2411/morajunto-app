import { useContext } from 'react'
import { MoradiaContext } from '../contexts/moradia-context'

export function useMoradia() {
    return useContext(MoradiaContext)
}