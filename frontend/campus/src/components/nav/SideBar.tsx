import { useRef, useEffect } from 'react';
import styled from 'styled-components';
import { Link } from 'react-router-dom';

import { FiChevronsLeft } from 'react-icons/fi';

const SideBarWrap = styled.div`
  height: 100%;
  width: 50%;
  background-color: #e7e4e1;
  border-radius: 0.5rem;
  position: fixed;
  top: 0;
  right: -55%;
  z-index: 5;
  transition: 0.5s ease;
  &.open {
    right: 0;
    transition: 0.5s ease;
  }
`;

function Sidebar({ isOpen, setIsOpen }: { isOpen: boolean; setIsOpen: any }) {
	const outside = useRef<any>();

	useEffect(() => {
		document.addEventListener('mousedown', handlerOutside);
		return () => {
			document.removeEventListener('mousedown', handlerOutside);
		};
	});

	const handlerOutside = (e: any) => {
		if (!outside.current.contains(e.target)) {
			toggleSide();
		}
	};

	const toggleSide = () => {
		setIsOpen(false);
	};

	return (
		<SideBarWrap id="sidebar" ref={outside} className={isOpen ? 'open' : ''}>
			<FiChevronsLeft
				onClick={toggleSide}
				onKeyDown={toggleSide}
			/>
			<ul>
				<li><Link to="/">홈</Link></li>
				<li><Link to="/us">실시간 소통</Link></li>
				<li><Link to="/deal">중고 거래</Link></li>
				<li><Link to="/mycamp">마이 페이지</Link></li>
				<li><Link to="/camps">캠핑장 정보</Link></li>
			</ul>
		</SideBarWrap>
	);
}

export default Sidebar;
